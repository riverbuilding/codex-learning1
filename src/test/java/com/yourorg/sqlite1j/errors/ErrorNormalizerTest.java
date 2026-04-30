package com.yourorg.sqlite1j.errors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorNormalizerTest {
    @Test
    void normalizesParseError() {
        DbError err = ErrorNormalizer.normalize("parse_fail", "syntax error near SELECT");
        assertEquals(ErrorCategory.PARSE, err.category());
        assertEquals("PARSE_ERROR", err.code());
    }

    @Test
    void normalizesSchemaErrorByMessage() {
        DbError err = ErrorNormalizer.normalize("unknown", "no such table: t");
        assertEquals(ErrorCategory.SCHEMA, err.category());
    }

    @Test
    void normalizesConstraintError() {
        DbError err = ErrorNormalizer.normalize("constraint_violation", "UNIQUE constraint failed");
        assertEquals(ErrorCategory.CONSTRAINT, err.category());
    }

    @Test
    void normalizesTransactionError() {
        DbError err = ErrorNormalizer.normalize("txn_state", "transaction not active");
        assertEquals(ErrorCategory.TRANSACTION, err.category());
    }

    @Test
    void normalizesStorageIoError() {
        DbError err = ErrorNormalizer.normalize("io_open", "unable to open database file");
        assertEquals(ErrorCategory.STORAGE_IO, err.category());
    }

    @Test
    void fallsBackToInternalError() {
        DbError err = ErrorNormalizer.normalize("mystery", "unexpected issue");
        assertEquals(ErrorCategory.INTERNAL, err.category());
        assertEquals("INTERNAL_ERROR", err.code());
    }
}
