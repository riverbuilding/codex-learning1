package com.yourorg.sqlite1j.storage;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagerTest {
    @Test
    void writesAndReadsPage() throws Exception {
        Path file = Files.createTempFile("pager", ".db");
        byte[] payload = new byte[]{10, 20, 30, 40};

        try (Pager pager = new Pager(file, 256)) {
            Page page = new Page(1, 256);
            PageCodec.writeBytes(page, 0, payload);
            pager.writePage(page);

            Page read = pager.readPage(1);
            assertArrayEquals(payload, PageCodec.readBytes(read, 0, payload.length));
        }
    }

    @Test
    void rejectsInvalidPageNumber() throws Exception {
        Path file = Files.createTempFile("pager", ".db");
        try (Pager pager = new Pager(file, 256)) {
            assertThrows(IllegalArgumentException.class, () -> pager.readPage(0));
        }
    }

    @Test
    void zeroFillsUnreadTail() throws Exception {
        Path file = Files.createTempFile("pager", ".db");
        try (Pager pager = new Pager(file, 128)) {
            Page read = pager.readPage(1);
            assertEquals(128, read.size());
            byte[] zeros = new byte[16];
            assertArrayEquals(zeros, PageCodec.readBytes(read, 0, 16));
        }
    }

    @Test
    void allocatesAndReusesFreedPageNumbers() throws Exception {
        Path file = Files.createTempFile("pager", ".db");
        try (Pager pager = new Pager(file, 128)) {
            int p1 = pager.allocatePageNumber();
            int p2 = pager.allocatePageNumber();
            pager.freePageNumber(p2);
            assertEquals(p2, pager.allocatePageNumber());
            assertEquals(1, p1);
        }
    }

    @Test
    void supportsCrashHookSimulation() throws Exception {
        Path file = Files.createTempFile("pager", ".db");
        try (Pager pager = new Pager(file, 128)) {
            Page page = new Page(1, 128);
            pager.setCrashHook(Pager.CrashHook.BEFORE_WRITE);
            assertThrows(Exception.class, () -> pager.writePage(page));
        }
    }
}
