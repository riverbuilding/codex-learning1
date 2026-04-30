package com.yourorg.sqlite1j.errors;

import java.util.Objects;

public final class DbError {
    private final ErrorCategory category;
    private final String code;
    private final String message;

    public DbError(ErrorCategory category, String code, String message) {
        this.category = Objects.requireNonNull(category, "category");
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");
    }

    public ErrorCategory category() {
        return category;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return category + ":" + code + " - " + message;
    }
}
