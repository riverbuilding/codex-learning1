package com.yourorg.sqlite1j.planner;

import java.util.List;

public final class AggregateNode implements LogicalPlanNode {
    private final LogicalPlanNode input;
    private final List<String> projections;

    public AggregateNode(LogicalPlanNode input, List<String> projections) {
        this.input = input;
        this.projections = projections;
    }

    public LogicalPlanNode input() {
        return input;
    }

    public List<String> projections() {
        return projections;
    }

    @Override
    public String nodeType() {
        return "Aggregate";
    }
}
