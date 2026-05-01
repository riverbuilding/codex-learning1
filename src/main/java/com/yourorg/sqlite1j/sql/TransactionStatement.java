package com.yourorg.sqlite1j.sql;

public final class TransactionStatement implements Statement {
    private final TransactionCommand command;

    public TransactionStatement(TransactionCommand command) {
        this.command = command;
    }

    public TransactionCommand command() {
        return command;
    }
}
