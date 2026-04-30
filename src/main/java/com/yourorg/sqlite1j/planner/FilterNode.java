package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.WhereClause;

public final class FilterNode implements LogicalPlanNode {
    private final LogicalPlanNode input;
    private final WhereClause whereClause;

    public FilterNode(LogicalPlanNode input, WhereClause whereClause) {
        this.input = input;
        this.whereClause = whereClause;
    }

    public LogicalPlanNode input() {
        return input;
    }

    public WhereClause whereClause() {
        return whereClause;
    }

    @Override
    public String nodeType() {
        return "Filter";
    }
}
