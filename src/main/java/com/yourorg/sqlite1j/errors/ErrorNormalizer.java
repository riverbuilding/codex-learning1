package com.yourorg.sqlite1j.errors;

public final class ErrorNormalizer {
    private ErrorNormalizer() {
    }

    public static DbError normalize(String rawCode, String rawMessage) {
        String code = safeUpper(rawCode);
        String message = rawMessage == null ? "" : rawMessage;

        if (code.startsWith("PARSE") || message.toLowerCase().contains("syntax")) {
            return new DbError(ErrorCategory.PARSE, "PARSE_ERROR", message);
        }
        if (code.startsWith("SCHEMA") || message.toLowerCase().contains("no such table") || message.toLowerCase().contains("no such column")) {
            return new DbError(ErrorCategory.SCHEMA, "SCHEMA_ERROR", message);
        }
        if (code.startsWith("CONSTRAINT") || message.toLowerCase().contains("constraint")) {
            return new DbError(ErrorCategory.CONSTRAINT, "CONSTRAINT_ERROR", message);
        }
        if (code.startsWith("TXN") || code.startsWith("TRANSACTION") || message.toLowerCase().contains("transaction")) {
            return new DbError(ErrorCategory.TRANSACTION, "TRANSACTION_ERROR", message);
        }
        if (code.startsWith("IO") || code.startsWith("STORAGE") || message.toLowerCase().contains("disk i/o") || message.toLowerCase().contains("unable to open")) {
            return new DbError(ErrorCategory.STORAGE_IO, "STORAGE_IO_ERROR", message);
        }
        return new DbError(ErrorCategory.INTERNAL, "INTERNAL_ERROR", message);
    }

    private static String safeUpper(String rawCode) {
        return rawCode == null ? "" : rawCode.trim().toUpperCase();
    }
}
