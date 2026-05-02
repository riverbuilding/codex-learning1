package com.yourorg.sqlite1j.sql;

public record DeleteStatement(String tableName, WhereClause whereClause) implements Statement {
}
