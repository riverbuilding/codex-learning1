package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.Statement;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryDatabaseStatementDispatchTest {

    @Test
    void dispatchesCreateInsertAndSelectStatements() {
        InMemoryDatabase db = new InMemoryDatabase();
        Parser parser = new Parser();

        db.executeStatementNormalized(parser.parseCreateTable("CREATE TABLE users (name TEXT, age INTEGER);"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO users VALUES ('alice', 30);"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO users VALUES ('bob', 20);"));

        List<List<DbValue>> rows = db.executeStatementNormalized(parser.parseSelect("SELECT name FROM users WHERE age > 25;"));

        assertEquals(1, rows.size());
        assertEquals(DbValue.ofText("alice"), rows.get(0).get(0));
    }

    @Test
    void dispatchesTransactionStatements() {
        InMemoryDatabase db = new InMemoryDatabase();
        Parser parser = new Parser();

        db.executeStatementNormalized(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));
        db.executeStatementNormalized(parser.parseTransactionControl("BEGIN;"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO t VALUES ('alice', 30);"));
        db.executeStatementNormalized(parser.parseTransactionControl("ROLLBACK;"));

        List<List<DbValue>> rowsAfterRollback = db.executeStatementNormalized(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(0, rowsAfterRollback.size());

        db.executeStatementNormalized(parser.parseTransactionControl("BEGIN;"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO t VALUES ('bob', 20);"));
        db.executeStatementNormalized(parser.parseTransactionControl("COMMIT;"));

        List<List<DbValue>> rowsAfterCommit = db.executeStatementNormalized(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rowsAfterCommit.size());
        assertEquals(DbValue.ofText("bob"), rowsAfterCommit.get(0).get(0));
    }

    @Test
    void rejectsNestedBeginDeterministically() {
        InMemoryDatabase db = new InMemoryDatabase();
        Parser parser = new Parser();
        db.executeStatementNormalized(parser.parseTransactionControl("BEGIN;"));

        DbException error = assertThrows(DbException.class,
                () -> db.executeStatementNormalized(parser.parseTransactionControl("BEGIN;")));
        assertTrue(error.getMessage().contains("Nested transactions are not supported"));
    }

    @Test
    void throwsDeterministicErrorForUnknownStatementType() {
        InMemoryDatabase db = new InMemoryDatabase();

        DbException error = assertThrows(DbException.class,
                () -> db.executeStatementNormalized(new Statement() { }));

        assertTrue(error.getMessage().startsWith("PARSE:PARSE_ERROR - Unsupported statement type"));
    }
}
