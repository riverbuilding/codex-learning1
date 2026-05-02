package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.UpdateStatement;

import java.util.List;

public record UpdateNode(String tableName, List<UpdateStatement.Assignment> assignments) implements LogicalPlanNode {
    @Override
    public String nodeType() {
        return "Update";
    }
}
