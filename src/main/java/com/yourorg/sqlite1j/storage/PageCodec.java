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
        ByteBuffer buf = page.buffer().duplicate().order(ByteOrder.BIG_ENDIAN);
        buf.putInt(0, header.pageType());
        buf.putInt(4, header.cellCount());
        buf.putInt(8, header.freeStart());
    }

    public static PageHeader readHeader(Page page) {
        ByteBuffer buf = page.buffer().duplicate().order(ByteOrder.BIG_ENDIAN);
        int pageType = buf.getInt(0);
        int cellCount = buf.getInt(4);
        int freeStart = buf.getInt(8);
        return new PageHeader(pageType, cellCount, freeStart);
    }

    public static void writeBytes(Page page, int offset, byte[] data) {
        ByteBuffer buf = page.buffer().duplicate();
        buf.position(offset);
        buf.put(data);
    }

    public static byte[] readBytes(Page page, int offset, int length) {
        byte[] out = new byte[length];
        ByteBuffer buf = page.buffer().duplicate();
        buf.position(offset);
        buf.get(out);
        return out;
    }
}
