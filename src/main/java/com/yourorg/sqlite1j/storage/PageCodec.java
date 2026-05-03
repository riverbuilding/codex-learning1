package com.yourorg.sqlite1j.storage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PageCodec {
    private static final int HEADER_SIZE = 12;

    private PageCodec() {
    }

    public static int headerSize() {
        return HEADER_SIZE;
    }

    public static void writeHeader(Page page, PageHeader header) {
        if (page.size() < HEADER_SIZE) {
            throw new IllegalArgumentException("Page too small for header");
        }
        if (header.freeStart() < HEADER_SIZE || header.freeStart() > page.size()) {
            throw new IllegalArgumentException("Invalid freeStart in header: " + header.freeStart());
        }
        if (header.cellCount() < 0) {
            throw new IllegalArgumentException("Invalid negative cellCount");
        }
        ByteBuffer buf = page.buffer().duplicate().order(ByteOrder.BIG_ENDIAN);
        buf.putInt(0, header.pageType());
        buf.putInt(4, header.cellCount());
        buf.putInt(8, header.freeStart());
    }

    public static PageHeader readHeader(Page page) {
        if (page.size() < HEADER_SIZE) {
            throw new IllegalArgumentException("Page too small for header");
        }
        ByteBuffer buf = page.buffer().duplicate().order(ByteOrder.BIG_ENDIAN);
        int pageType = buf.getInt(0);
        int cellCount = buf.getInt(4);
        int freeStart = buf.getInt(8);
        if (freeStart < HEADER_SIZE || freeStart > page.size()) {
            throw new IllegalArgumentException("Corrupt header freeStart=" + freeStart);
        }
        if (cellCount < 0) {
            throw new IllegalArgumentException("Corrupt header cellCount=" + cellCount);
        }
        return new PageHeader(pageType, cellCount, freeStart);
    }

    public static void writeBytes(Page page, int offset, byte[] data) {
        if (offset < 0 || data.length < 0 || offset + data.length > page.size()) {
            throw new IllegalArgumentException("writeBytes out of bounds");
        }
        ByteBuffer buf = page.buffer().duplicate();
        buf.position(offset);
        buf.put(data);
    }

    public static byte[] readBytes(Page page, int offset, int length) {
        if (offset < 0 || length < 0 || offset + length > page.size()) {
            throw new IllegalArgumentException("readBytes out of bounds");
        }
        byte[] out = new byte[length];
        ByteBuffer buf = page.buffer().duplicate();
        buf.position(offset);
        buf.get(out);
        return out;
    }
}
