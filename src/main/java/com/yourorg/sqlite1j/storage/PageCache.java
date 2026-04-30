package com.yourorg.sqlite1j.storage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PageCache {
    private final Pager pager;
    private final int capacity;
    private final LinkedHashMap<Integer, Page> cache;
    private long hitCount;
    private long missCount;

    public PageCache(Pager pager, int capacity) {
        this.pager = pager;
        this.capacity = capacity;
        this.cache = new LinkedHashMap<Integer, Page>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Page> eldest) {
                return size() > PageCache.this.capacity;
            }
        };
    }

    public synchronized Page get(int pageNumber) throws IOException {
        Page existing = cache.get(pageNumber);
        if (existing != null) {
            hitCount++;
            return existing;
        }

        missCount++;
        Page loaded = pager.readPage(pageNumber);
        cache.put(pageNumber, loaded);
        return loaded;
    }

    public synchronized void put(Page page) throws IOException {
        cache.put(page.pageNumber(), page);
        pager.writePage(page);
    }

    public synchronized int size() {
        return cache.size();
    }

    public synchronized long hitCount() {
        return hitCount;
    }

    public synchronized long missCount() {
        return missCount;
    }
}
