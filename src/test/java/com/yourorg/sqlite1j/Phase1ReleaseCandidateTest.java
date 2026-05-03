package com.yourorg.sqlite1j;

import com.yourorg.sqlite1j.exec.InMemoryDatabase;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Phase1ReleaseCandidateTest {
    @Test
    void phase1RcCoreFlowAndTransactionGuardrails() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE users (id INTEGER, name TEXT);"));
        db.execute(parser.parseInsert("INSERT INTO users VALUES (1, 'ann');"));
        db.execute(parser.parseInsert("INSERT INTO users VALUES (2, 'bob');"));

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM users WHERE id > 1;"));
        assertEquals(1, rows.size());
        assertEquals("bob", rows.get(0).get(0).asText());

        db.beginTransaction();
        IllegalStateException txError = assertThrows(IllegalStateException.class, db::beginTransaction);
        assertTrue(txError.getMessage().contains("Nested transactions are not supported"));
        db.rollbackTransaction();
    }
}
