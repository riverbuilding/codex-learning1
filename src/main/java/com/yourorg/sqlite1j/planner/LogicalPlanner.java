package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.sql.DeleteStatement;
import com.yourorg.sqlite1j.sql.UpdateStatement;
import com.yourorg.sqlite1j.sql.WhereClause;

public final class LogicalPlanner {
    public LogicalPlanNode planSelect(BoundSelect boundSelect) {
        SelectStatement stmt = boundSelect.statement();
        LogicalPlanNode current = scanFor(boundSelect);

        if (stmt.whereClause() != null) {
            current = new FilterNode(current, stmt.whereClause());
        }
        if (containsAggregate(stmt.projections())) {
            current = new AggregateNode(current, stmt.projections());
        } else {
            current = new ProjectNode(current, stmt.projections());
        }
        if (!stmt.orderBy().isEmpty()) {
            current = new SortNode(current, stmt.orderBy());
        }
        if (stmt.limit() != null) {
            current = new LimitNode(current, stmt.limit());
        }
        return current;
    }

    private LogicalPlanNode scanFor(BoundSelect boundSelect) {
        WhereClause where = boundSelect.statement().whereClause();
        if (where != null && "=".equals(where.operator()) && boundSelect.table().hasIndexOn(where.column())) {
            return new IndexScanNode(boundSelect.table().name(), where.column());
        }
        return new TableScanNode(boundSelect.table().name());
    }

    public LogicalPlanNode planInsert(InsertStatement insert) {
        return new InsertNode(insert.tableName(), insert.values());
    }

    public LogicalPlanNode planUpdate(UpdateStatement update) {
        return new UpdateNode(update.tableName(), update.assignments());
    }

    public LogicalPlanNode planDelete(DeleteStatement delete) {
        return new DeleteNode(delete.tableName());
    }

    private boolean containsAggregate(java.util.List<String> projections) {
        for (String projection : projections) {
            String upper = projection.toUpperCase();
            if (upper.startsWith("COUNT(") || upper.startsWith("MIN(") || upper.startsWith("MAX(")) {
                return true;
            }
        }
        return false;
    }
}
