package com.yourorg.sqlite1j.sql;

import java.util.List;

public final class SelectStatement implements Statement {
    public static final class OrderByTerm {
        private final String column;
        private final boolean ascending;

        public OrderByTerm(String column, boolean ascending) {
            this.column = column;
            this.ascending = ascending;
        }

        public String column() {
            return column;
        }

        public boolean ascending() {
            return ascending;
        }
    }

    private final List<String> projections;
    private final String fromTable;
    private final WhereClause whereClause;
    private final List<OrderByTerm> orderBy;
    private final Integer limit;

    public SelectStatement(List<String> projections, String fromTable, WhereClause whereClause, List<OrderByTerm> orderBy, Integer limit) {
        this.projections = projections;
        this.fromTable = fromTable;
        this.whereClause = whereClause;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    public List<String> projections() {
        return projections;
    }

    public String fromTable() {
        return fromTable;
    }

    public WhereClause whereClause() {
        return whereClause;
    }

    public List<OrderByTerm> orderBy() {
        return orderBy;
    }

    public Integer limit() {
        return limit;
    }
}
