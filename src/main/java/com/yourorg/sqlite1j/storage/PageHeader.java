package com.yourorg.sqlite1j.storage;

public final class PageHeader {
    private final int pageType;
    private final int cellCount;
    private final int freeStart;

    public PageHeader(int pageType, int cellCount, int freeStart) {
        this.pageType = pageType;
        this.cellCount = cellCount;
        this.freeStart = freeStart;
    }

    public int pageType() {
        return pageType;
    }

    public int cellCount() {
        return cellCount;
    }

    public int freeStart() {
        return freeStart;
    }
}
