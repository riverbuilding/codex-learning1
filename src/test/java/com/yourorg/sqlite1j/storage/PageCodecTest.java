package com.yourorg.sqlite1j.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void rejectsCorruptHeaderBounds() {
        Page page = new Page(1, 64);
        PageCodec.writeBytes(page, 8, new byte[]{0, 0, 1, 0}); // freeStart=256 corrupt
        assertThrows(IllegalArgumentException.class, () -> PageCodec.readHeader(page));
    }

    @Test
    void rejectsOutOfBoundsPayloadAccess() {
        Page page = new Page(1, 32);
        assertThrows(IllegalArgumentException.class, () -> PageCodec.writeBytes(page, 31, new byte[]{1, 2}));
        assertThrows(IllegalArgumentException.class, () -> PageCodec.readBytes(page, 31, 2));
    }
}
