package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryDatabaseTransactionTest {
    @Test
    void rollbackRestoresPreviousState() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES ('alice', 30);"));

        db.beginTransaction();
        db.execute(parser.parseInsert("INSERT INTO t VALUES ('bob', 20);"));
        db.rollbackTransaction();

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get(0).asText());
    }

    @Test
    void commitPersistsChanges() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));

        db.beginTransaction();
        db.execute(parser.parseInsert("INSERT INTO t VALUES ('alice', 30);"));
        db.commitTransaction();

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get(0).asText());
    }
}
