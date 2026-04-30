package com.yourorg.sqlite1j.storage;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Pager implements Closeable {
    private final FileChannel channel;
    private final int pageSize;

    public Pager(Path file, int pageSize) throws IOException {
        this(file, pageSize, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    public Pager(Path file, int pageSize, OpenOption... options) throws IOException {
        this.channel = FileChannel.open(file, options);
        this.pageSize = pageSize;
    }

    public Page readPage(int pageNumber) throws IOException {
        long offset = offsetOf(pageNumber);
        ByteBuffer buffer = ByteBuffer.allocate(pageSize);
        channel.position(offset);
        int read = channel.read(buffer);
        if (read < 0) {
            while (buffer.position() < pageSize) {
                buffer.put((byte) 0);
            }
        } else if (read < pageSize) {
            while (buffer.position() < pageSize) {
                buffer.put((byte) 0);
            }
        }
        buffer.flip();
        return new Page(pageNumber, buffer);
    }

    public void writePage(Page page) throws IOException {
        if (page.size() != pageSize) {
            throw new IllegalArgumentException("Unexpected page size: " + page.size() + ", expected " + pageSize);
        }
        long offset = offsetOf(page.pageNumber());
        ByteBuffer data = page.buffer().duplicate();
        data.clear();
        channel.position(offset);
        while (data.hasRemaining()) {
            channel.write(data);
        }
        channel.force(false);
    }

    private long offsetOf(int pageNumber) {
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Page numbers are 1-based");
        }
        return (long) (pageNumber - 1) * pageSize;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
