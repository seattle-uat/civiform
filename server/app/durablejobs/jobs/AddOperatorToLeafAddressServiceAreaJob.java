package durablejobs.jobs;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import durablejobs.DurableJob;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.SqlRow;
import io.ebean.Transaction;
import io.ebean.TxScope;
import java.util.List;
import java.util.Objects;
import models.PersistedDurableJobModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.program.predicate.Operator;

public final class AddOperatorToLeafAddressServiceAreaJob extends DurableJob {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AddOperatorToLeafAddressServiceAreaJob.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final Database database;
  private final PersistedDurableJobModel persistedDurableJobModel;

  public AddOperatorToLeafAddressServiceAreaJob(PersistedDurableJobModel persistedDurableJobModel) {
    this.persistedDurableJobModel = checkNotNull(persistedDurableJobModel);
    this.database = DB.getDefault();
  }

  @Override
  public PersistedDurableJobModel getPersistedDurableJob() {
    return persistedDurableJobModel;
  }

  @Override
  public void run() {
    LOGGER.debug("Run - Begin");

    String selectSql =
        """
SELECT id, block_definitions
FROM programs
WHERE jsonb_path_exists(block_definitions, '$.hidePredicate.rootNode.**.node ? (@.type == "leafAddressServiceArea")')
OR jsonb_path_exists(block_definitions, '$.eligibilityDefinition.predicate.rootNode.**.node ? (@.type == "leafAddressServiceArea")')
""";

    List<SqlRow> programs = database.sqlQuery(selectSql).findList();

    try (Transaction jobTransaction = database.beginTransaction()) {
      jobTransaction.setNestedUseSavepoint();
      int errorCount = 0;

      for (SqlRow program : programs) {
        LOGGER.debug("id: {}", program.getLong("id"));

        try {
          JsonNode rootJsonNode = objectMapper.readTree(program.getString("block_definitions"));
          int startingRootJsonNodeHashCode = rootJsonNode.hashCode();

          if (!rootJsonNode.isArray()) {
            LOGGER.error("block_definitions is not an array");
            continue;
          }

          for (var blockDefinitionJsonNode : rootJsonNode) {
            JsonNode nodeJsonNode =
                blockDefinitionJsonNode.at("/eligibilityDefinition/predicate/rootNode/node");
            if (!nodeJsonNode.isMissingNode()) {
              addOperatorToLeafAddressServiceAreaNode(nodeJsonNode);
            }
          }

          for (var blockDefinitionJsonNode : rootJsonNode) {
            JsonNode nodeJsonNode = blockDefinitionJsonNode.at("/hidePredicate/rootNode/node");
            if (!nodeJsonNode.isMissingNode()) {
              addOperatorToLeafAddressServiceAreaNode(nodeJsonNode);
            }
          }

          if (startingRootJsonNodeHashCode == rootJsonNode.hashCode()) {
            LOGGER.debug("No changes made to JsonNode. No need to update the database.");
            continue;
          }

          String updateSql =
              """
              update programs
              set block_definitions = CAST(:block_definitions AS jsonb)
              where id = :id
              """;

          try (Transaction stepTransaction = database.beginTransaction(TxScope.mandatory())) {
            database
                .sqlUpdate(updateSql)
                .setParameter("id", program.getLong("id"))
                .setParameter("block_definitions", rootJsonNode.toString())
                .execute();

            stepTransaction.commit();
            LOGGER.debug("JsonNode change. Updated database.");
          }
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          errorCount++;
        }
      }

      if (errorCount == 0) {
        LOGGER.debug("Job succeeded.");
        jobTransaction.commit();
      } else {
        LOGGER.error("Job failed to add operator. All changes undone. Error count: {}", errorCount);
        jobTransaction.rollback();
      }
    }

    LOGGER.debug("Run - End");
  }

  public void addOperatorToLeafAddressServiceAreaNode(JsonNode nodeJsonNode) {
    if (!nodeJsonNode.isObject()) {
      return;
    }

    if (nodeJsonNode.has("children") && nodeJsonNode.get("children").isArray()) {
      for (JsonNode childNodeJsonNode : nodeJsonNode.get("children")) {
        if (childNodeJsonNode.has("node")) {
          addOperatorToLeafAddressServiceAreaNode(childNodeJsonNode.get("node"));
        }
      }
    } else if (nodeJsonNode.has("type")
        && Objects.equals(nodeJsonNode.get("type").textValue(), "leafAddressServiceArea")
        && !nodeJsonNode.has("operator")) {
      ((ObjectNode) nodeJsonNode).put("operator", Operator.IN_SERVICE_AREA.name());
    }
  }
}
