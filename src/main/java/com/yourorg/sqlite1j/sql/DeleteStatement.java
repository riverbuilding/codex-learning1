package com.yourorg.sqlite1j.sql;

public final class DeleteStatement implements Statement {
    private final String tableName;
    private final WhereClause whereClause;

    public DeleteStatement(String tableName, WhereClause whereClause) {
        this.tableName = tableName;
        this.whereClause = whereClause;
    }

    public String tableName() {
        return tableName;
    }

    public WhereClause whereClause() {
        return whereClause;
    }
}
