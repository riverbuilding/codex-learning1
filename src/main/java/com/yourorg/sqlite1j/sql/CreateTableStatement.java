package com.yourorg.sqlite1j.sql;

import java.util.List;

public final class CreateTableStatement implements Statement {
    private final String tableName;
    private final List<ColumnDef> columns;

    public CreateTableStatement(String tableName, List<ColumnDef> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String tableName() {
        return tableName;
    }

    public List<ColumnDef> columns() {
        return columns;
    }
}
