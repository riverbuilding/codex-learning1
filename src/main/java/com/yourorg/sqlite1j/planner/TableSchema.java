package com.yourorg.sqlite1j.planner;

import java.util.HashSet;
import java.util.Set;

public final class TableSchema {
    private final String name;
    private final Set<String> columns;
    private final Set<String> indexedColumns;

    public TableSchema(String name, Set<String> columns) {
        this(name, columns, java.util.Collections.<String>emptySet());
    }

    public TableSchema(String name, Set<String> columns, Set<String> indexedColumns) {
        this.name = name;
        this.columns = new HashSet<>(columns);
        this.indexedColumns = new HashSet<>(indexedColumns);
    }

    public String name() {
        return name;
    }

    public boolean hasColumn(String column) {
        return columns.contains(column);
    }

    public boolean hasIndexOn(String column) {
        return indexedColumns.contains(column);
    }
}
