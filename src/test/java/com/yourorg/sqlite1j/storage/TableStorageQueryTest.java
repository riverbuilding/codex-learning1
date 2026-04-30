package com.yourorg.sqlite1j.storage;

import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableStorageQueryTest {
    @Test
    void scansAndFiltersStoredRows() {
        TableStorage storage = new TableStorage("users", List.of("name", "age"));
        storage.insert(List.of(DbValue.ofText("alice"), DbValue.ofInteger(30)));
        storage.insert(List.of(DbValue.ofText("bob"), DbValue.ofInteger(20)));
        storage.insert(List.of(DbValue.ofText("carol"), DbValue.ofInteger(35)));

        Parser parser = new Parser();
        List<List<DbValue>> result = storage.select(parser.parseSelect("SELECT name FROM users WHERE age > 25;"));

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).get(0).asText());
        assertEquals("carol", result.get(1).get(0).asText());
    }

    @Test
    void selectStarReturnsAllColumns() {
        TableStorage storage = new TableStorage("t", List.of("a", "b"));
        storage.insert(List.of(DbValue.ofInteger(1), DbValue.ofInteger(2)));

        Parser parser = new Parser();
        List<List<DbValue>> result = storage.select(parser.parseSelect("SELECT * FROM t;"));

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(1L, result.get(0).get(0).asInteger());
        assertEquals(2L, result.get(0).get(1).asInteger());
    }
}
