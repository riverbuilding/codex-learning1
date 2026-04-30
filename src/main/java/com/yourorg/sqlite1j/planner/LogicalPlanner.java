package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.SelectStatement;

public final class LogicalPlanner {
    public LogicalPlanNode planSelect(BoundSelect boundSelect) {
        SelectStatement stmt = boundSelect.statement();
        LogicalPlanNode current = new TableScanNode(boundSelect.table().name());

        if (stmt.whereClause() != null) {
            current = new FilterNode(current, stmt.whereClause());
        }

        return new ProjectNode(current, stmt.projections());
    }

    public LogicalPlanNode planInsert(InsertStatement insert) {
        return new InsertNode(insert.tableName(), insert.values());
    }
}
