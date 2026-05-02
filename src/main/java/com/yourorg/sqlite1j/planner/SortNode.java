package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.SelectStatement;

import java.util.List;

public final class SortNode implements LogicalPlanNode {
    private final LogicalPlanNode input;
    private final List<SelectStatement.OrderByTerm> terms;

    public SortNode(LogicalPlanNode input, List<SelectStatement.OrderByTerm> terms) {
        this.input = input;
        this.terms = terms;
    }

    public LogicalPlanNode input() {
        return input;
    }

    public List<SelectStatement.OrderByTerm> terms() {
        return terms;
    }

    @Override
    public String nodeType() {
        return "Sort";
    }
}
