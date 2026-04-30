package com.yourorg.sqlite1j.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserInsertTest {
    @Test
    void parsesInsertWithMixedLiterals() {
        InsertStatement stmt = new Parser().parseInsert("INSERT INTO users VALUES ('alice', 42);");

        assertEquals("users", stmt.tableName());
        assertEquals(2, stmt.values().size());
        assertEquals("alice", stmt.values().get(0));
        assertEquals("42", stmt.values().get(1));
    }

    @Test
    void parsesInsertWithoutSemicolon() {
        InsertStatement stmt = new Parser().parseInsert("INSERT INTO t VALUES (1)");
        assertEquals("t", stmt.tableName());
        assertEquals(1, stmt.values().size());
    }

    @Test
    void failsOnMissingValuesKeyword() {
        assertThrows(IllegalArgumentException.class,
                () -> new Parser().parseInsert("INSERT INTO t (1)")
        );
    }
}
