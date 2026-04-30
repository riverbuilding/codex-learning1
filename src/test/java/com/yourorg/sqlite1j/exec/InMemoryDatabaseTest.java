package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryDatabaseTest {
    @Test
    void createInsertSelectEndToEnd() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE users (name TEXT, age INTEGER);"));
        db.execute(parser.parseInsert("INSERT INTO users VALUES ('alice', 30);"));
        db.execute(parser.parseInsert("INSERT INTO users VALUES ('bob', 20);"));

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM users WHERE age > 25;"));
        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get(0).asText());
    }

    @Test
    void supportsSelectStar() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE t (a INT, b INT);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (1, 2);"));

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT * FROM t;"));
        assertEquals(1, rows.size());
        assertEquals(2, rows.get(0).size());
    }
}
