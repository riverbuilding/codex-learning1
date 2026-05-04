package com.yourorg.sqlite1j.planner;

public final class IndexScanNode implements LogicalPlanNode {
    private final String tableName;
    private final String columnName;

    public IndexScanNode(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String tableName() {
        return tableName;
    }

    public String columnName() {
        return columnName;
    }

    @Override
    public String nodeType() {
        return "IndexScan";
    }
}
