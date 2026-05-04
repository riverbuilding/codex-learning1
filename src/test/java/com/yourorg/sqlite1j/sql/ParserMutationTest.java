package com.yourorg.sqlite1j.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserMutationTest {
    @Test
    void parsesUpdateWithAssignmentsAndWhere() {
        UpdateStatement stmt = new Parser().parseUpdate("UPDATE users SET name='bob', age=22 WHERE id = 1;");
        assertEquals("users", stmt.tableName());
        assertEquals(2, stmt.assignments().size());
        assertEquals("name", stmt.assignments().get(0).column());
        assertEquals("bob", stmt.assignments().get(0).literal());
        assertEquals("id", stmt.whereClause().column());
    }

    @Test
    void parsesDeleteWithWhere() {
        DeleteStatement stmt = new Parser().parseDelete("DELETE FROM users WHERE age >= 18;");
        assertEquals("users", stmt.tableName());
        assertEquals("age", stmt.whereClause().column());
        assertEquals(">=", stmt.whereClause().operator());
        assertEquals("18", stmt.whereClause().literal());
    }

    @Test
    void parsesCreateIndex() {
        CreateIndexStatement stmt = new Parser().parseCreateIndex("CREATE INDEX idx_users_age ON users (age);");
        assertEquals("idx_users_age", stmt.indexName());
        assertEquals("users", stmt.tableName());
        assertEquals("age", stmt.columnName());
    }
}
