package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.UpdateStatement;

import java.util.List;

public final class UpdateNode implements LogicalPlanNode {
    private final String tableName;
    private final List<UpdateStatement.Assignment> assignments;

    public UpdateNode(String tableName, List<UpdateStatement.Assignment> assignments) {
        this.tableName = tableName;
        this.assignments = assignments;
    }

    public String tableName() {
        return tableName;
    }

    public List<UpdateStatement.Assignment> assignments() {
        return assignments;
    }

    @Override
    public String nodeType() {
        return "Update";
    }
}
