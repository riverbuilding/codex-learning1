package com.yourorg.sqlite1j.planner;

public final class TableScanNode implements LogicalPlanNode {
    private final String tableName;

    public TableScanNode(String tableName) {
        this.tableName = tableName;
    }

    public String tableName() {
        return tableName;
    }

    @Override
    public String nodeType() {
        return "TableScan";
    }
}
