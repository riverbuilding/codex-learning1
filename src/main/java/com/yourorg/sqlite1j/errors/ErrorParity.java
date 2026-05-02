package com.yourorg.sqlite1j.errors;

public final class ErrorParity {
    private ErrorParity() {
    }

    public static DbError normalizeThrowable(Throwable t) {
        if (t == null) {
            return new DbError(ErrorCategory.INTERNAL, "INTERNAL_ERROR", "unknown error");
        }

        String msg = t.getMessage() == null ? "" : t.getMessage();
        String lower = msg.toLowerCase();

        if (t instanceof IllegalArgumentException) {
            if (lower.contains("unknown table") || lower.contains("unknown column") || lower.contains("arity")
                    || lower.contains("value count does not match column count") || lower.contains("column count")
                    || lower.contains("unsupported aggregate")) {
                return new DbError(ErrorCategory.SCHEMA, "SCHEMA_ERROR", msg);
            }
            if (lower.contains("expected") || lower.contains("unterminated") || lower.contains("unexpected character")
                    || lower.contains("unsupported statement type")) {
                return new DbError(ErrorCategory.PARSE, "PARSE_ERROR", msg);
            }
        }

        if (t instanceof IllegalStateException && lower.contains("state")) {
            return new DbError(ErrorCategory.TRANSACTION, "TRANSACTION_ERROR", msg);
        }

        return ErrorNormalizer.normalize("", msg);
    }
}
