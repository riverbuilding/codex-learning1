package com.yourorg.sqlite1j.sql;

import java.util.List;

public final class SelectStatement {
    private final List<String> projections;
    private final String fromTable;
    private final WhereClause whereClause;

    public SelectStatement(List<String> projections, String fromTable, WhereClause whereClause) {
        this.projections = projections;
        this.fromTable = fromTable;
        this.whereClause = whereClause;
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
}
