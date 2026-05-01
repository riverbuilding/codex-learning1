package com.yourorg.sqlite1j.sql;

import java.util.List;

public final class InsertStatement implements Statement {
    private final String tableName;
    private final List<String> values;

    public InsertStatement(String tableName, List<String> values) {
        this.tableName = tableName;
        this.values = values;
    }

    public String tableName() {
        return tableName;
    }

    public List<String> values() {
        return values;
    }
}
