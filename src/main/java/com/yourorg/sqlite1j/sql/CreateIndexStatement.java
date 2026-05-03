package com.yourorg.sqlite1j.sql;

public final class CreateIndexStatement implements Statement {
    private final String indexName;
    private final String tableName;
    private final String columnName;

    public CreateIndexStatement(String indexName, String tableName, String columnName) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String indexName() {
        return indexName;
    }

    public String tableName() {
        return tableName;
    }

    public String columnName() {
        return columnName;
    }
}
