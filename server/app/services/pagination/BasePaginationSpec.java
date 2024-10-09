package services.pagination;

import io.ebean.ExpressionList;

public abstract class BasePaginationSpec {
  private final int pageSize;

  public BasePaginationSpec(int pageSize) {
    this.pageSize = pageSize;
  }

  public int getPageSize() {
    return this.pageSize;
  }

  public final <T> ExpressionList<T> apply(ExpressionList<T> query) {
    query = this.applySetMaxRows(query);
    query = this.applyOrderBy(query);
    query = this.maybeApplyWhere(query);
    query = this.maybeApplySetFirstRow(query);
    return query;
  }

  private final <T> ExpressionList<T> applySetMaxRows(ExpressionList<T> query) {
    return query.setMaxRows(this.pageSize);
  }

  protected abstract <T> ExpressionList<T> applyOrderBy(ExpressionList<T> query);

  protected <T> ExpressionList<T> maybeApplyWhere(ExpressionList<T> query) {
    return query;
  }

  protected <T> ExpressionList<T> maybeApplySetFirstRow(ExpressionList<T> query) {
    return query;
  }
}
