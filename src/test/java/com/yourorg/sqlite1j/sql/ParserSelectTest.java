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
        assertEquals(0, stmt.orderBy().size());
        assertNull(stmt.limit());
    }

    @Test
    void parsesSelectStarWithoutWhere() {
        SelectStatement stmt = new Parser().parseSelect("SELECT * FROM t");
        assertEquals(1, stmt.projections().size());
        assertEquals("*", stmt.projections().get(0));
        assertEquals("t", stmt.fromTable());
        assertNull(stmt.whereClause());
        assertEquals(0, stmt.orderBy().size());
        assertNull(stmt.limit());
    }

    @Test
    void parsesOrderByAndLimit() {
        SelectStatement stmt = new Parser().parseSelect("SELECT id FROM users ORDER BY name DESC, id ASC LIMIT 10;");
        assertEquals(2, stmt.orderBy().size());
        assertEquals("name", stmt.orderBy().get(0).column());
        assertEquals(false, stmt.orderBy().get(0).ascending());
        assertEquals("id", stmt.orderBy().get(1).column());
        assertEquals(true, stmt.orderBy().get(1).ascending());
        assertEquals(10, stmt.limit());
    }

    @Test
    void parsesAggregateProjections() {
        SelectStatement stmt = new Parser().parseSelect("SELECT COUNT(*), COUNT(id), MIN(age), MAX(age) FROM users;");
        assertEquals("COUNT(*)", stmt.projections().get(0));
        assertEquals("COUNT(id)", stmt.projections().get(1));
        assertEquals("MIN(age)", stmt.projections().get(2));
        assertEquals("MAX(age)", stmt.projections().get(3));
    }

    @Test
    void parsesSelectWithExtendedComparisonOperators() {
        SelectStatement notEquals = new Parser().parseSelect("SELECT id FROM users WHERE id != 1;");
        assertEquals("!=", notEquals.whereClause().operator());

        SelectStatement lessOrEqual = new Parser().parseSelect("SELECT id FROM users WHERE id <= 2;");
        assertEquals("<=", lessOrEqual.whereClause().operator());

        SelectStatement greaterOrEqual = new Parser().parseSelect("SELECT id FROM users WHERE id >= 3;");
        assertEquals(">=", greaterOrEqual.whereClause().operator());
    }

    @Test
    void failsWhenFromMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> new Parser().parseSelect("SELECT id users"));
    }

    @Test
    void parsesSelectLiteralAliasWithoutFrom() {
        SelectStatement stmt = new Parser().parseSelect("SELECT 1 AS value;");
        assertEquals(1, stmt.projections().size());
        assertEquals("value", stmt.projections().get(0));
        assertEquals("1", stmt.literalProjections().get(0));
    }
}
