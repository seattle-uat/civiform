package services.applicant;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Locale;
import java.util.Optional;
import models.Applicant;
import models.LifecycleStage;
import org.junit.Before;
import org.junit.Test;
import repository.WithPostgresContainer;
import services.program.ProgramDefinition;
import services.question.types.QuestionDefinition;
import support.ProgramBuilder;
import support.TestQuestionBank;

public class ReadOnlyApplicantProgramServiceImplTest extends WithPostgresContainer {

  private QuestionDefinition nameQuestion;
  private QuestionDefinition colorQuestion;
  private QuestionDefinition addressQuestion;
  private ApplicantData applicantData;
  private ProgramDefinition programDefinition;
  private ReadOnlyApplicantProgramService subject;

  @Before
  public void setUp() {
    applicantData = new ApplicantData();
    nameQuestion = TestQuestionBank.applicantName().getQuestionDefinition();
    colorQuestion = TestQuestionBank.applicantFavoriteColor().getQuestionDefinition();
    addressQuestion = TestQuestionBank.applicantAddress().getQuestionDefinition();
    programDefinition =
        ProgramBuilder.newProgram()
            .withBlock("Block one")
            .withQuestionDefinition(nameQuestion)
            .withBlock("Block two")
            .withQuestionDefinition(colorQuestion)
            .withQuestionDefinition(addressQuestion)
            .buildDefinition();
    subject = new ReadOnlyApplicantProgramServiceImpl(applicantData, programDefinition);
  }

  @Test
  public void getCurrentBlockList_getsTheApplicantSpecificBlocksForTheProgram() {
    ImmutableList<Block> blockList = subject.getCurrentBlockList();

    assertThat(blockList).hasSize(2);
    Block block = blockList.get(0);
    assertThat(block.getName()).isEqualTo("Block one");
  }

  @Test
  public void getCurrentBlockList_doesNotIncludeCompleteBlocks() {
    // Answer block one questions
    answerNameQuestion();

    ImmutableList<Block> blockList = subject.getCurrentBlockList();

    assertThat(blockList).hasSize(1);
    assertThat(blockList.get(0).getName()).isEqualTo("Block two");
  }

  @Test
  public void getCurrentBlockList_returnsEmptyListIfAllBlocksCompletedInAnotherProgram() {
    // Answer all questions for a different program.
    answerNameQuestion(88L);
    answerColorQuestion(88L);
    answerAddressQuestion(88L);

    ImmutableList<Block> blockList = subject.getCurrentBlockList();

    assertThat(blockList).isEmpty();
  }

  @Test
  public void getCurrentBlockList_includesBlocksThatWereCompletedInThisProgram() {
    ImmutableList<Block> blockList = subject.getCurrentBlockList();
    assertThat(blockList).hasSize(2);

    // Answer block 1 questions in this program session
    answerNameQuestion(programDefinition.id());
    blockList = subject.getCurrentBlockList();

    // Block 1 should still be there
    assertThat(blockList).hasSize(2);
    Block block = blockList.get(0);
    assertThat(block.getName()).isEqualTo("Block one");
  }

  @Test
  public void getCurrentBlockList_includesBlocksThatWerePartiallyCompletedInAnotherProgram() {
    // Answer one of block 2 questions in another program
    answerAddressQuestion(88L);
    ImmutableList<Block> blockList = subject.getCurrentBlockList();
    assertThat(blockList).hasSize(2);

    // Answer the other block 2 questions in this program session
    answerColorQuestion(programDefinition.id());
    blockList = subject.getCurrentBlockList();

    // Block 1 should still be there
    assertThat(blockList).hasSize(2);
    Block block = blockList.get(0);
    assertThat(block.getName()).isEqualTo("Block one");
  }

  @Test
  public void getBlock_blockExists_returnsTheBlock() {
    Optional<Block> maybeBlock = subject.getBlock("1");

    assertThat(maybeBlock).isPresent();
    assertThat(maybeBlock.get().getId()).isEqualTo("1");
  }

  @Test
  public void getBlock_blockNotInList_returnsEmpty() {
    Optional<Block> maybeBlock = subject.getBlock("111");

    assertThat(maybeBlock).isEmpty();
  }

  @Test
  public void getBlockAfter_thereExistsABlockAfter_returnsTheBlockAfterTheGivenBlock() {
    Optional<Block> maybeBlock = subject.getBlockAfter("1");

    assertThat(maybeBlock).isPresent();
    assertThat(maybeBlock.get().getId()).isEqualTo("2");
  }

  @Test
  public void getBlockAfter_argIsLastBlock_returnsEmpty() {
    Optional<Block> maybeBlock = subject.getBlockAfter("321");

    assertThat(maybeBlock).isEmpty();
  }

  @Test
  public void getBlockAfter_emptyBlockList_returnsEmpty() {
    subject =
        new ReadOnlyApplicantProgramServiceImpl(
            new Applicant().getApplicantData(),
            ProgramDefinition.builder()
                .setId(123L)
                .setName("Admin program name")
                .addLocalizedName(Locale.US, "The Program")
                .addLocalizedDescription(Locale.US, "This program is for testing.")
                .setLifecycleStage(LifecycleStage.ACTIVE)
                .build());

    Optional<Block> maybeBlock = subject.getBlockAfter("321");

    assertThat(maybeBlock).isEmpty();
  }

  @Test
  public void getFirstIncompleteBlock_firstIncompleteBlockReturned() {
    Optional<Block> maybeBlock = subject.getFirstIncompleteBlock();

    assertThat(maybeBlock).isNotEmpty();
    assertThat(maybeBlock.get().getName()).isEqualTo("Block one");
  }

  @Test
  public void getFirstIncompleteBlock_returnsFirstIncompleteIfFirstBlockCompleted() {
    // Answer the first block in this program - it will still be in getCurrentBlockList;
    answerNameQuestion(programDefinition.id());
    assertThat(subject.getCurrentBlockList().get(0).getName()).isEqualTo("Block one");

    Optional<Block> maybeBlock = subject.getFirstIncompleteBlock();

    assertThat(maybeBlock).isNotEmpty();
    assertThat(maybeBlock.get().getName()).isEqualTo("Block two");
  }

  private void answerNameQuestion() {
    answerNameQuestion(1L);
  }

  private void answerNameQuestion(long programId) {
    applicantData.putString(nameQuestion.getPath().join("first"), "Alice");
    applicantData.putString(nameQuestion.getPath().join("middle"), "Middle");
    applicantData.putString(nameQuestion.getPath().join("last"), "Last");
    applicantData.putLong(nameQuestion.getProgramIdPath(), programId);
    applicantData.putLong(nameQuestion.getLastUpdatedTimePath(), 12345L);
  }

  private void answerColorQuestion(long programId) {
    applicantData.putString(colorQuestion.getPath().join("text"), "mauve");
    applicantData.putLong(colorQuestion.getProgramIdPath(), programId);
    applicantData.putLong(colorQuestion.getLastUpdatedTimePath(), 12345L);
  }

  private void answerAddressQuestion(long programId) {
    applicantData.putString(addressQuestion.getPath().join("street"), "123 Rhode St.");
    applicantData.putString(addressQuestion.getPath().join("city"), "Seattle");
    applicantData.putString(addressQuestion.getPath().join("state"), "WA");
    applicantData.putString(addressQuestion.getPath().join("zip"), "12345");
    applicantData.putLong(addressQuestion.getProgramIdPath(), programId);
    applicantData.putLong(addressQuestion.getLastUpdatedTimePath(), 12345L);
  }
}
