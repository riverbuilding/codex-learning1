package com.yourorg.sqlite1j.storage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BTreeTest {
    @Test
    void insertsAndSearchesByKey() {
        BTree tree = new BTree(4);
        tree.insert(10, new byte[]{1});
        tree.insert(20, new byte[]{2, 3});

        assertArrayEquals(new byte[]{1}, tree.search(10));
        assertArrayEquals(new byte[]{2, 3}, tree.search(20));
        assertNull(tree.search(99));
    }

    @Test
    void returnsOrderedKeys() {
        BTree tree = new BTree(4);
        tree.insert(30, new byte[]{3});
        tree.insert(10, new byte[]{1});
        tree.insert(20, new byte[]{2});

        assertEquals(List.of(10L, 20L, 30L), tree.orderedKeys());
        assertEquals(3, tree.size());
    }

    @Test
    void valueIsDefensivelyCopied() {
        BTree tree = new BTree(4);
        byte[] value = new byte[]{9, 8};
        tree.insert(1, value);
        value[0] = 0;

        byte[] read = tree.search(1);
        assertArrayEquals(new byte[]{9, 8}, read);
        read[1] = 0;
        assertArrayEquals(new byte[]{9, 8}, tree.search(1));
    }

    @Test
    void splitsLeafWhenCapacityExceeded() {
        BTree tree = new BTree(2);
        tree.insert(1, new byte[]{1});
        tree.insert(2, new byte[]{2});
        tree.insert(3, new byte[]{3}); // triggers split

        assertTrue(tree.splitCount() >= 1);
        assertTrue(tree.leafCount() >= 2);
        assertEquals(List.of(1L, 2L, 3L), tree.orderedKeys());
    }
}
