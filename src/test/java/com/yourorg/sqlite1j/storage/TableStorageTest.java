package com.yourorg.sqlite1j.storage;

import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableStorageTest {
    @Test
    void insertsAndScansRowsInOrder() {
        TableStorage storage = new TableStorage("users", List.of("name", "age"));

        storage.insert(List.of(DbValue.ofText("alice"), DbValue.ofInteger(30)));
        storage.insert(List.of(DbValue.ofText("bob"), DbValue.ofInteger(20)));

        List<Map<String, DbValue>> rows = storage.scanAll();
        assertEquals(2, rows.size());
        assertEquals("alice", rows.get(0).get("name").asText());
        assertEquals(30L, rows.get(0).get("age").asInteger());
        assertEquals("bob", rows.get(1).get("name").asText());
    }

    @Test
    void rejectsArityMismatch() {
        TableStorage storage = new TableStorage("t", List.of("a", "b"));
        try {
            storage.insert(List.of(DbValue.ofInteger(1)));
        } catch (IllegalArgumentException ex) {
            assertEquals("Arity mismatch for table t", ex.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
