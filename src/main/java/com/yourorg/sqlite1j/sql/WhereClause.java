package com.yourorg.sqlite1j.sql;

public final class WhereClause {
    private final String column;
    private final String operator;
    private final String literal;

    public WhereClause(String column, String operator, String literal) {
        this.column = column;
        this.operator = operator;
        this.literal = literal;
    }

    public String column() {
        return column;
    }

    public String operator() {
        return operator;
    }

    public String literal() {
        return literal;
    }
}
