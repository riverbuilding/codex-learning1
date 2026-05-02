package com.yourorg.sqlite1j.sql;

import java.util.List;

public final class UpdateStatement implements Statement {
    private final String tableName;
    private final List<Assignment> assignments;
    private final WhereClause whereClause;

    public UpdateStatement(String tableName, List<Assignment> assignments, WhereClause whereClause) {
        this.tableName = tableName;
        this.assignments = assignments;
        this.whereClause = whereClause;
    }

    public String tableName() {
        return tableName;
    }

    public List<Assignment> assignments() {
        return assignments;
    }

    public WhereClause whereClause() {
        return whereClause;
    }

    public static final class Assignment {
        private final String column;
        private final String literal;

        public Assignment(String column, String literal) {
            this.column = column;
            this.literal = literal;
        }

        public String column() {
            return column;
        }

        public String literal() {
            return literal;
        }
    }
}
