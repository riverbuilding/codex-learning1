package com.yourorg.sqlite1j.errors;

public final class DbException extends RuntimeException {
    private final DbError error;

    public DbException(DbError error) {
        super(error.toString());
        this.error = error;
    }

    public DbError error() {
        return error;
    }
}
