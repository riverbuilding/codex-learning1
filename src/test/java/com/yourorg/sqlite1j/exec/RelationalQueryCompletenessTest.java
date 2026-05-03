package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RelationalQueryCompletenessTest {
    @Test
    void supportsInnerJoinExecution() {
        Parser p = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(p.parseCreateTable("CREATE TABLE users (id INT, name TEXT);"));
        db.execute(p.parseCreateTable("CREATE TABLE posts (id INT, user_id INT);"));
        db.execute(p.parseInsert("INSERT INTO users VALUES (1, 'alice');"));
        db.execute(p.parseInsert("INSERT INTO users VALUES (2, 'bob');"));
        db.execute(p.parseInsert("INSERT INTO posts VALUES (10, 1);"));

        List<List<DbValue>> rows = db.execute(p.parseSelect("SELECT name FROM users INNER JOIN posts ON id = user_id;"));
        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get(0).asText());
    }

    @Test
    void supportsScopedSubqueryInFrom() {
        Parser p = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(p.parseCreateTable("CREATE TABLE users (id INT, name TEXT);"));
        db.execute(p.parseInsert("INSERT INTO users VALUES (1, 'alice');"));

        List<List<DbValue>> rows = db.execute(p.parseSelect("SELECT name FROM (SELECT name FROM users) u;"));
        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get(0).asText());
    }

    @Test
    void normalizesAmbiguousColumnAsSchemaError() {
        Parser p = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(p.parseCreateTable("CREATE TABLE a (id INT);"));
        db.execute(p.parseCreateTable("CREATE TABLE b (id INT);"));
        db.execute(p.parseInsert("INSERT INTO a VALUES (1);"));
        db.execute(p.parseInsert("INSERT INTO b VALUES (1);"));

        DbException ex = assertThrows(DbException.class,
                () -> db.executeStatementNormalized(p.parseSelect("SELECT id FROM a JOIN b ON id = id;")));
        assertEquals("SCHEMA_ERROR", ex.error().code());
    }
}
