package com.yourorg.sqlite1j.errors;

import com.yourorg.sqlite1j.exec.InMemoryDatabase;
import com.yourorg.sqlite1j.exec.SqlCommandRunner;
import com.yourorg.sqlite1j.sql.Parser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorNormalizationIntegrationTest {
    @Test
    void normalizesNestedBeginAsTransactionError() {
        SqlCommandRunner runner = new SqlCommandRunner(new Parser(), new InMemoryDatabase());
        runner.execute("BEGIN;");

        DbException error = assertThrows(DbException.class, () -> runner.execute("BEGIN;"));
        assertEquals(ErrorCategory.TRANSACTION, error.error().category());
        assertEquals("TRANSACTION_ERROR", error.error().code());
    }

    @Test
    void normalizesUnknownTableAsSchemaError() {
        SqlCommandRunner runner = new SqlCommandRunner(new Parser(), new InMemoryDatabase());

        DbException error = assertThrows(DbException.class, () -> runner.execute("SELECT id FROM missing;"));
        assertEquals(ErrorCategory.SCHEMA, error.error().category());
    }

    @Test
    void normalizesInsertArityAsSchemaError() {
        SqlCommandRunner runner = new SqlCommandRunner(new Parser(), new InMemoryDatabase());
        runner.execute("CREATE TABLE t (id INTEGER, name TEXT);");

        DbException error = assertThrows(DbException.class, () -> runner.execute("INSERT INTO t VALUES (1);") );
        assertEquals(ErrorCategory.SCHEMA, error.error().category());
    }

    @Test
    void normalizesMalformedSqlAsParseError() {
        SqlCommandRunner runner = new SqlCommandRunner(new Parser(), new InMemoryDatabase());

        DbException error = assertThrows(DbException.class, () -> runner.execute("SELECT id users"));
        assertEquals(ErrorCategory.PARSE, error.error().category());
    }

    @Test
    void normalizesMalformedLimitAsParseError() {
        SqlCommandRunner runner = new SqlCommandRunner(new Parser(), new InMemoryDatabase());
        runner.execute("CREATE TABLE t (id INTEGER);");

        DbException error = assertThrows(DbException.class, () -> runner.execute("SELECT id FROM t LIMIT 'x';"));
        assertEquals(ErrorCategory.PARSE, error.error().category());
    }

    @Test
    void normalizesAggregateMixWithoutGroupingAsSchemaError() {
        SqlCommandRunner runner = new SqlCommandRunner(new Parser(), new InMemoryDatabase());
        runner.execute("CREATE TABLE t (id INTEGER);");
        runner.execute("INSERT INTO t VALUES (1);");

        DbException error = assertThrows(DbException.class, () -> runner.execute("SELECT COUNT(*), id FROM t;"));
        assertEquals(ErrorCategory.SCHEMA, error.error().category());
    }
}
