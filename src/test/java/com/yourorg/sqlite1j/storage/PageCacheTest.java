package com.yourorg.sqlite1j.storage;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageCacheTest {
    @Test
    void recordsHitAndMissAndEvictsLru() throws Exception {
        Path file = Files.createTempFile("pagecache", ".db");

        try (Pager pager = new Pager(file, 128)) {
            PageCache cache = new PageCache(pager, 2);

            cache.get(1); // miss
            cache.get(2); // miss
            cache.get(1); // hit
            cache.get(3); // miss, evicts 2
            cache.get(2); // miss (since evicted)

            assertEquals(1L, cache.hitCount());
            assertEquals(4L, cache.missCount());
            assertEquals(2, cache.size());
        }
    }

    @Test
    void putWritesThroughPager() throws Exception {
        Path file = Files.createTempFile("pagecache", ".db");

        try (Pager pager = new Pager(file, 128)) {
            PageCache cache = new PageCache(pager, 2);
            Page p1 = new Page(1, 128);
            PageCodec.writeBytes(p1, 0, new byte[]{9, 8, 7});
            cache.put(p1);

            Page read = pager.readPage(1);
            assertEquals(9, PageCodec.readBytes(read, 0, 1)[0]);
        }
    }
}
