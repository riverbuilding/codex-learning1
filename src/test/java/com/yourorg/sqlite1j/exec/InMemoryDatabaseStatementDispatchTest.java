package com.yourorg.sqlite1j.exec;

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

        db.executeStatement(parser.parseCreateTable("CREATE TABLE users (name TEXT, age INTEGER);"));
        db.executeStatement(parser.parseInsert("INSERT INTO users VALUES ('alice', 30);"));
        db.executeStatement(parser.parseInsert("INSERT INTO users VALUES ('bob', 20);"));

        List<List<DbValue>> rows = db.executeStatement(parser.parseSelect("SELECT name FROM users WHERE age > 25;"));

        assertEquals(1, rows.size());
        assertEquals(DbValue.ofText("alice"), rows.get(0).get(0));
    }

    @Test
    void dispatchesTransactionStatements() {
        InMemoryDatabase db = new InMemoryDatabase();
        Parser parser = new Parser();

        db.executeStatement(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));
        db.executeStatement(parser.parseTransactionControl("BEGIN;"));
        db.executeStatement(parser.parseInsert("INSERT INTO t VALUES ('alice', 30);"));
        db.executeStatement(parser.parseTransactionControl("ROLLBACK;"));

        List<List<DbValue>> rowsAfterRollback = db.executeStatement(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(0, rowsAfterRollback.size());

        db.executeStatement(parser.parseTransactionControl("BEGIN;"));
        db.executeStatement(parser.parseInsert("INSERT INTO t VALUES ('bob', 20);"));
        db.executeStatement(parser.parseTransactionControl("COMMIT;"));

        List<List<DbValue>> rowsAfterCommit = db.executeStatement(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rowsAfterCommit.size());
        assertEquals(DbValue.ofText("bob"), rowsAfterCommit.get(0).get(0));
    }

    @Test
    void throwsDeterministicErrorForUnknownStatementType() {
        InMemoryDatabase db = new InMemoryDatabase();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> db.executeStatement(new Statement() { }));

        assertTrue(error.getMessage().startsWith("Unsupported statement type:"));
    }
}
