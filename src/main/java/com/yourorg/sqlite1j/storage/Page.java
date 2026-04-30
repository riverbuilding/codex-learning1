package com.yourorg.sqlite1j.storage;

import java.nio.ByteBuffer;

public final class Page {
    private final int pageNumber;
    private final ByteBuffer buffer;

    public Page(int pageNumber, int pageSize) {
        this(pageNumber, ByteBuffer.allocate(pageSize));
    }

    public Page(int pageNumber, ByteBuffer buffer) {
        this.pageNumber = pageNumber;
        this.buffer = buffer;
    }

    public int pageNumber() {
        return pageNumber;
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    public int size() {
        return buffer.capacity();
    }
}
