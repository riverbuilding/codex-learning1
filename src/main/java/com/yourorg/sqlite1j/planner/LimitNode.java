package com.yourorg.sqlite1j.planner;

public final class LimitNode implements LogicalPlanNode {
    private final LogicalPlanNode input;
    private final int limit;

    public LimitNode(LogicalPlanNode input, int limit) {
        this.input = input;
        this.limit = limit;
    }

    public LogicalPlanNode input() {
        return input;
    }

    public int limit() {
        return limit;
    }

    @Override
    public String nodeType() {
        return "Limit";
    }
}
