package com.yourorg.sqlite1j.planner;

import java.util.HashMap;
import java.util.Map;

public final class SchemaCatalog {
    private final Map<String, TableSchema> tables = new HashMap<>();

    public void register(TableSchema table) {
        tables.put(table.name(), table);
    }

    public TableSchema findTable(String tableName) {
        return tables.get(tableName);
    }
}
