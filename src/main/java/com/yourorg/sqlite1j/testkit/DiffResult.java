package com.yourorg.sqlite1j.testkit;

import java.util.List;

public final class DiffResult {
    private final boolean matches;
    private final List<String> differences;

    public DiffResult(boolean matches, List<String> differences) {
        this.matches = matches;
        this.differences = differences;
    }

    public boolean matches() {
        return matches;
    }

    public List<String> differences() {
        return differences;
    }
}
