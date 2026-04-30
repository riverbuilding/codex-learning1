package com.yourorg.sqlite1j.errors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorParityTest {
    @Test
    void normalizesParseLikeIllegalArgument() {
        DbError e = ErrorParity.normalizeThrowable(new IllegalArgumentException("Expected keyword 'FROM'"));
        assertEquals(ErrorCategory.PARSE, e.category());
        assertEquals("PARSE_ERROR", e.code());
    }

    @Test
    void normalizesSchemaLikeIllegalArgument() {
        DbError e = ErrorParity.normalizeThrowable(new IllegalArgumentException("Unknown table: users"));
        assertEquals(ErrorCategory.SCHEMA, e.category());
        assertEquals("SCHEMA_ERROR", e.code());
    }

    @Test
    void normalizesTransactionState() {
        DbError e = ErrorParity.normalizeThrowable(new IllegalStateException("COMMIT requires ACTIVE state"));
        assertEquals(ErrorCategory.TRANSACTION, e.category());
        assertEquals("TRANSACTION_ERROR", e.code());
    }

    @Test
    void fallsBackToNormalizer() {
        DbError e = ErrorParity.normalizeThrowable(new RuntimeException("disk i/o failure"));
        assertEquals(ErrorCategory.STORAGE_IO, e.category());
    }
}
