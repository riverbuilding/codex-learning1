package com.yourorg.sqlite1j.testkit;

public final class GoldenCase {
    private final String name;
    private final String script;
    private final ExecutionResult expected;

    public GoldenCase(String name, String script, ExecutionResult expected) {
        this.name = name;
        this.script = script;
        this.expected = expected;
    }

    public String name() {
        return name;
    }

    public String script() {
        return script;
    }

    public ExecutionResult expected() {
        return expected;
    }
}
