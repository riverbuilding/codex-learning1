package com.yourorg.sqlite1j.storage;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Pager implements Closeable {
    public enum CrashHook {
        NONE,
        BEFORE_WRITE,
        AFTER_WRITE_BEFORE_FORCE
    }

    private final FileChannel channel;
    private final int pageSize;
    private int highestAllocatedPage;
    private final java.util.Deque<Integer> freeList = new java.util.ArrayDeque<>();
    private CrashHook crashHook = CrashHook.NONE;

    public Pager(Path file, int pageSize) throws IOException {
        this(file, pageSize, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    public Pager(Path file, int pageSize, OpenOption... options) throws IOException {
        this.channel = FileChannel.open(file, options);
        this.pageSize = pageSize;
        this.highestAllocatedPage = (int) (channel.size() / pageSize);
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
        if (crashHook == CrashHook.BEFORE_WRITE) {
            throw new IOException("Simulated crash before page write");
        }
        long offset = offsetOf(page.pageNumber());
        ByteBuffer data = page.buffer().duplicate();
        data.clear();
        channel.position(offset);
        while (data.hasRemaining()) {
            channel.write(data);
        }
        if (page.pageNumber() > highestAllocatedPage) {
            highestAllocatedPage = page.pageNumber();
        }
        if (crashHook == CrashHook.AFTER_WRITE_BEFORE_FORCE) {
            throw new IOException("Simulated crash after page write before force");
        }
        channel.force(false);
    }

    public synchronized int allocatePageNumber() {
        if (!freeList.isEmpty()) {
            return freeList.pop();
        }
        highestAllocatedPage++;
        return highestAllocatedPage;
    }

    public synchronized void freePageNumber(int pageNumber) {
        if (pageNumber <= 0 || pageNumber > highestAllocatedPage) {
            throw new IllegalArgumentException("Cannot free out-of-range page number: " + pageNumber);
        }
        if (freeList.contains(pageNumber)) {
            throw new IllegalArgumentException("Page number already freed: " + pageNumber);
        }
        freeList.push(pageNumber);
    }

    public synchronized void setCrashHook(CrashHook crashHook) {
        this.crashHook = crashHook == null ? CrashHook.NONE : crashHook;
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
