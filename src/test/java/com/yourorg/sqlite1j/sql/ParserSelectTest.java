package com.yourorg.sqlite1j.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserSelectTest {
    @Test
    void parsesSelectWithWhere() {
        SelectStatement stmt = new Parser().parseSelect("SELECT id, name FROM users WHERE id = 1;");

        assertEquals(2, stmt.projections().size());
        assertEquals("id", stmt.projections().get(0));
        assertEquals("name", stmt.projections().get(1));
        assertEquals("users", stmt.fromTable());
        assertEquals("id", stmt.whereClause().column());
        assertEquals("=", stmt.whereClause().operator());
        assertEquals("1", stmt.whereClause().literal());
    }

    @Test
    void parsesSelectStarWithoutWhere() {
        SelectStatement stmt = new Parser().parseSelect("SELECT * FROM t");
        assertEquals(1, stmt.projections().size());
        assertEquals("*", stmt.projections().get(0));
        assertEquals("t", stmt.fromTable());
        assertNull(stmt.whereClause());
    }

    @Test
    void failsWhenFromMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> new Parser().parseSelect("SELECT id users"));
    }
}
