package com.yourorg.sqlite1j.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public final class BTree {
    private final NavigableMap<Long, byte[]> index = new TreeMap<>();

    public void insert(long key, byte[] value) {
        index.put(key, value.clone());
    }

    public byte[] search(long key) {
        byte[] value = index.get(key);
        return value == null ? null : value.clone();
    }

    public List<Long> orderedKeys() {
        return new ArrayList<>(index.navigableKeySet());
    }

    public int size() {
        return index.size();
    }
}
