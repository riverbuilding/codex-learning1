package com.yourorg.sqlite1j.planner;

public final class DeleteNode implements LogicalPlanNode {
    private final String tableName;

    public DeleteNode(String tableName) {
        this.tableName = tableName;
    }

    public String tableName() {
        return tableName;
    }

    @Override
    public String nodeType() {
        return "Delete";
    }
}
