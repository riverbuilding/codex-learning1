package com.yourorg.sqlite1j.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserCreateTableTest {
    @Test
    void parsesCreateTableWithTwoColumns() {
        CreateTableStatement stmt = new Parser().parseCreateTable("CREATE TABLE users (id INTEGER, name TEXT);");

        assertEquals("users", stmt.tableName());
        assertEquals(2, stmt.columns().size());
        assertEquals("id", stmt.columns().get(0).name());
        assertEquals("INTEGER", stmt.columns().get(0).typeName());
        assertEquals("name", stmt.columns().get(1).name());
        assertEquals("TEXT", stmt.columns().get(1).typeName());
    }

    @Test
    void parsesWithoutSemicolon() {
        CreateTableStatement stmt = new Parser().parseCreateTable("CREATE TABLE t (a INT)");
        assertEquals("t", stmt.tableName());
        assertEquals(1, stmt.columns().size());
    }

    @Test
    void failsOnMissingType() {
        assertThrows(IllegalArgumentException.class,
                () -> new Parser().parseCreateTable("CREATE TABLE t (a);")
        );
    }
}
