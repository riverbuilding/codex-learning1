package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.sql.WhereClause;

public final class NameBinder {
    public BoundSelect bindSelect(SelectStatement statement, SchemaCatalog catalog) {
        TableSchema table = catalog.findTable(statement.fromTable());
        if (table == null) {
            throw new IllegalArgumentException("Unknown table: " + statement.fromTable());
        }

        for (String projection : statement.projections()) {
            String aggregateColumn = aggregateColumn(projection);
            if (aggregateColumn != null) {
                if (!"*".equals(aggregateColumn) && !table.hasColumn(aggregateColumn)) {
                    throw new IllegalArgumentException("Unknown column in aggregate: " + aggregateColumn);
                }
                continue;
            }
            if (!"*".equals(projection) && !table.hasColumn(projection)) {
                throw new IllegalArgumentException("Unknown column in projection: " + projection);
            }
        }
        ensureSupportedAggregateProjectionMix(statement.projections());

        WhereClause where = statement.whereClause();
        if (where != null && !table.hasColumn(where.column())) {
            throw new IllegalArgumentException("Unknown column in WHERE: " + where.column());
        }
        for (SelectStatement.OrderByTerm term : statement.orderBy()) {
            if (!table.hasColumn(term.column())) {
                throw new IllegalArgumentException("Unknown column in ORDER BY: " + term.column());
            }
        }

        return new BoundSelect(statement, table);
    }

    private void ensureSupportedAggregateProjectionMix(java.util.List<String> projections) {
        boolean hasAggregate = false;
        boolean hasNonAggregate = false;
        for (String projection : projections) {
            if (aggregateColumn(projection) != null) {
                hasAggregate = true;
            } else {
                hasNonAggregate = true;
            }
        }
        if (hasAggregate && hasNonAggregate) {
            throw new IllegalArgumentException("Unsupported aggregate/non-aggregate projection mix without GROUP BY");
        }
    }

    private String aggregateColumn(String projection) {
        String upper = projection.toUpperCase();
        if (upper.startsWith("COUNT(") || upper.startsWith("MIN(") || upper.startsWith("MAX(")) {
            int open = projection.indexOf('(');
            int close = projection.lastIndexOf(')');
            if (open < 0 || close <= open) {
                throw new IllegalArgumentException("Malformed aggregate projection: " + projection);
            }
            return projection.substring(open + 1, close).trim();
        }
        return null;
    }
}
