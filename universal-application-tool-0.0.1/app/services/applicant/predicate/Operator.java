package services.applicant.predicate;

public enum Operator {
  ANY_OF("anyof"),
  EQUAL_TO("=="),
  GREATER_THAN(">"),
  GREATER_THAN_OR_EQUAL_TO(">="),
  IN("in"),
  LESS_THAN("<"),
  LESS_THAN_OR_EQUAL_TO("<="),
  NONE_OF("noneof"),
  NOT_EQUAL_TO("!="),
  NOT_IN("nin"),
  SUBSET_OF("subsetof");

  private final String jsonPathOperator;

  Operator(String jsonPathOperator) {
    this.jsonPathOperator = jsonPathOperator;
  }

  public String toJsonPathOperator() {
    return this.jsonPathOperator;
  }
}
