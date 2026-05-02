package com.yourorg.sqlite1j.planner;

public record DeleteNode(String tableName) implements LogicalPlanNode {
    @Override
    public String nodeType() {
        return "Delete";
    }
}
