package com.yourorg.sqlite1j.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PageCodecTest {
    @Test
    void roundTripsHeaderFields() {
        Page page = new Page(1, 256);
        PageHeader header = new PageHeader(2, 10, 64);

        PageCodec.writeHeader(page, header);
        PageHeader read = PageCodec.readHeader(page);

        assertEquals(2, read.pageType());
        assertEquals(10, read.cellCount());
        assertEquals(64, read.freeStart());
    }

    @Test
    void readsAndWritesPayloadBytes() {
        Page page = new Page(2, 256);
        byte[] payload = new byte[]{1, 2, 3, 4};

        PageCodec.writeBytes(page, PageCodec.headerSize(), payload);
        byte[] read = PageCodec.readBytes(page, PageCodec.headerSize(), payload.length);

        assertArrayEquals(payload, read);
    }
}
