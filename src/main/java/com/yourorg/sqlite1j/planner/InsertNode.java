package com.yourorg.sqlite1j.planner;

import java.util.List;

public final class InsertNode implements LogicalPlanNode {
    private final String tableName;
    private final List<String> values;

    public InsertNode(String tableName, List<String> values) {
        this.tableName = tableName;
        this.values = values;
    }

    public String tableName() {
        return tableName;
    }

    public List<String> values() {
        return values;
    }

    @Override
    public String nodeType() {
        return "Insert";
    }
}
