package com.yourorg.sqlite1j.planner;

import com.yourorg.sqlite1j.sql.SelectStatement;

public final class BoundSelect {
    private final SelectStatement statement;
    private final TableSchema table;

    public BoundSelect(SelectStatement statement, TableSchema table) {
        this.statement = statement;
        this.table = table;
    }

    public SelectStatement statement() {
        return statement;
    }

    public TableSchema table() {
        return table;
    }
}
