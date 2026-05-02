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
            if (!"*".equals(projection) && !table.hasColumn(projection)) {
                throw new IllegalArgumentException("Unknown column in projection: " + projection);
            }
        }

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
}
