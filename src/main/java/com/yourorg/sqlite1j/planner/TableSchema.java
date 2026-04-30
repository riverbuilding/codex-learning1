package com.yourorg.sqlite1j.planner;

import java.util.HashSet;
import java.util.Set;

public final class TableSchema {
    private final String name;
    private final Set<String> columns;

    public TableSchema(String name, Set<String> columns) {
        this.name = name;
        this.columns = new HashSet<>(columns);
    }

    public String name() {
        return name;
    }

    public boolean hasColumn(String column) {
        return columns.contains(column);
    }
}
