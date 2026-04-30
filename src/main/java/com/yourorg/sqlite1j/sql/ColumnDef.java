package com.yourorg.sqlite1j.sql;

public final class ColumnDef {
    private final String name;
    private final String typeName;

    public ColumnDef(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    public String name() {
        return name;
    }

    public String typeName() {
        return typeName;
    }
}
