package com.yourorg.sqlite1j.sql;

import java.util.List;

public record UpdateStatement(String tableName, List<Assignment> assignments, WhereClause whereClause) implements Statement {
    public record Assignment(String column, String literal) {
    }
}
