package com.yourorg.sqlite1j.sql;

import java.util.List;

public final class SelectStatement implements Statement {
    public static final class OrderByTerm {
        private final String column;
        private final boolean ascending;
        public OrderByTerm(String column, boolean ascending) { this.column = column; this.ascending = ascending; }
        public String column() { return column; }
        public boolean ascending() { return ascending; }
    }

    public static final class FromItem {
        private final String tableName;
        private final SelectStatement subquery;
        private final String alias;
        public FromItem(String tableName, SelectStatement subquery, String alias) {
            this.tableName = tableName; this.subquery = subquery; this.alias = alias;
        }
        public static FromItem table(String tableName, String alias) { return new FromItem(tableName, null, alias); }
        public static FromItem subquery(SelectStatement subquery, String alias) { return new FromItem(null, subquery, alias); }
        public boolean isSubquery() { return subquery != null; }
        public String tableName() { return tableName; }
        public SelectStatement subquery() { return subquery; }
        public String alias() { return alias; }
    }

    public static final class JoinClause {
        private final FromItem right;
        private final String leftColumn;
        private final String rightColumn;
        public JoinClause(FromItem right, String leftColumn, String rightColumn) {
            this.right = right; this.leftColumn = leftColumn; this.rightColumn = rightColumn;
        }
        public FromItem right() { return right; }
        public String leftColumn() { return leftColumn; }
        public String rightColumn() { return rightColumn; }
    }

    private final List<String> projections;
    private final FromItem from;
    private final List<JoinClause> joins;
    private final WhereClause whereClause;
    private final List<OrderByTerm> orderBy;
    private final Integer limit;

    public SelectStatement(List<String> projections, FromItem from, List<JoinClause> joins, WhereClause whereClause, List<OrderByTerm> orderBy, Integer limit) {
        this.projections = projections; this.from = from; this.joins = joins; this.whereClause = whereClause; this.orderBy = orderBy; this.limit = limit;
    }
    public List<String> projections() { return projections; }
    public String fromTable() { return from.tableName(); }
    public FromItem from() { return from; }
    public List<JoinClause> joins() { return joins; }
    public WhereClause whereClause() { return whereClause; }
    public List<OrderByTerm> orderBy() { return orderBy; }
    public Integer limit() { return limit; }
}
