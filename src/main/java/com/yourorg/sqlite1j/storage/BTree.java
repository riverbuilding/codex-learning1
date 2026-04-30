package com.yourorg.sqlite1j.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class BTree {
    private final int maxEntriesPerNode;
    private final List<LeafNode> leaves = new ArrayList<>();
    private int splitCount;

    public BTree() {
        this(64);
    }

    public BTree(int maxEntriesPerNode) {
        this.maxEntriesPerNode = maxEntriesPerNode;
        this.leaves.add(new LeafNode());
    }

    public void insert(long key, byte[] value) {
        LeafNode leaf = findLeafForKey(key);
        leaf.entries.put(key, value.clone());

        if (leaf.entries.size() > maxEntriesPerNode) {
            splitLeaf(leaf);
        }
    }

    public byte[] search(long key) {
        LeafNode leaf = findLeafForKey(key);
        byte[] value = leaf.entries.get(key);
        return value == null ? null : value.clone();
    }

    public List<Long> orderedKeys() {
        List<Long> keys = new ArrayList<>();
        for (LeafNode leaf : leaves) {
            keys.addAll(leaf.entries.navigableKeySet());
        }
        return keys;
    }

    public int size() {
        int count = 0;
        for (LeafNode leaf : leaves) {
            count += leaf.entries.size();
        }
        return count;
    }

    public int splitCount() {
        return splitCount;
    }

    public int leafCount() {
        return leaves.size();
    }

    private LeafNode findLeafForKey(long key) {
        for (LeafNode leaf : leaves) {
            if (leaf.entries.isEmpty()) {
                return leaf;
            }
            Long first = leaf.entries.firstKey();
            Long last = leaf.entries.lastKey();
            if (key >= first && key <= last) {
                return leaf;
            }
            if (key < first) {
                return leaf;
            }
        }
        return leaves.get(leaves.size() - 1);
    }

    private void splitLeaf(LeafNode leaf) {
        int splitIndex = leaf.entries.size() / 2;
        LeafNode right = new LeafNode();

        int i = 0;
        List<Long> keys = new ArrayList<>(leaf.entries.navigableKeySet());
        for (Long key : keys) {
            if (i++ >= splitIndex) {
                right.entries.put(key, leaf.entries.remove(key));
            }
        }

        int idx = leaves.indexOf(leaf);
        leaves.add(idx + 1, right);
        splitCount++;
    }

    private static final class LeafNode {
        private final NavigableMap<Long, byte[]> entries = new TreeMap<>();
    }
}
