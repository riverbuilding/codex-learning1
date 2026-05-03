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

    @Test
    void supportsUpdateAndDeleteHeavySequence() {
        TableStorage storage = new TableStorage("users", List.of("name", "age"));
        long id1 = storage.insert(List.of(DbValue.ofText("a"), DbValue.ofInteger(1)));
        long id2 = storage.insert(List.of(DbValue.ofText("b"), DbValue.ofInteger(2)));
        long id3 = storage.insert(List.of(DbValue.ofText("c"), DbValue.ofInteger(3)));

        assertEquals(true, storage.updateByRowId(id2, List.of(DbValue.ofText("bb"), DbValue.ofInteger(20))));
        assertEquals(true, storage.deleteByRowId(id1));
        assertEquals(true, storage.deleteByRowId(id3));
        assertEquals(false, storage.deleteByRowId(999));

        List<Map<String, DbValue>> rows = storage.scanAll();
        assertEquals(1, rows.size());
        assertEquals("bb", rows.get(0).get("name").asText());
        assertEquals(20L, rows.get(0).get("age").asInteger());
    }
}
