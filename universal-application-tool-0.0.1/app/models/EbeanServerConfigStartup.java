package models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.ebean.config.DatabaseConfig;
import io.ebean.event.ServerConfigStartup;

/**
 * Provides a Jackson {@link ObjectMapper} that understands how to (de)serialize Guava types and
 * Java 8 Optionals. Note that this is necessary because Ebean uses a different ObjectMapper than
 * the one provided by the Play framework.
 */
public class EbeanServerConfigStartup implements ServerConfigStartup {

  @Override
  public void onStart(DatabaseConfig databaseConfig) {
    ObjectMapper mapper =
        new ObjectMapper().registerModule(new GuavaModule()).registerModule(new Jdk8Module());
    databaseConfig.setObjectMapper(mapper);
  }
}
