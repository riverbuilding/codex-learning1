package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryDatabaseMutationTest {
    @Test
    void executesUpdateAndTracksAffectedRows() {
        InMemoryDatabase db = new InMemoryDatabase();
        Parser parser = new Parser();
        db.executeStatementNormalized(parser.parseCreateTable("CREATE TABLE users (id INTEGER, name TEXT);"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO users VALUES (1, 'alice');"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO users VALUES (2, 'bob');"));

        db.executeStatementNormalized(parser.parseUpdate("UPDATE users SET name = 'ALICE' WHERE id = 1;"));
        assertEquals(1, db.lastMutationCount());

        List<List<DbValue>> rows = db.executeStatementNormalized(parser.parseSelect("SELECT name FROM users WHERE id = 1;"));
        assertEquals(DbValue.ofText("ALICE"), rows.get(0).get(0));
    }

    @Test
    void executesDeleteAndTracksAffectedRows() {
        InMemoryDatabase db = new InMemoryDatabase();
        Parser parser = new Parser();
        db.executeStatementNormalized(parser.parseCreateTable("CREATE TABLE users (id INTEGER, name TEXT);"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO users VALUES (1, 'alice');"));
        db.executeStatementNormalized(parser.parseInsert("INSERT INTO users VALUES (2, 'bob');"));

        db.executeStatementNormalized(parser.parseDelete("DELETE FROM users WHERE id >= 1;"));
        assertEquals(2, db.lastMutationCount());
        assertEquals(0, db.executeStatementNormalized(parser.parseSelect("SELECT * FROM users;")).size());
    }
}
