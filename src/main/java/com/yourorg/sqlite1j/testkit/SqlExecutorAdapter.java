package com.yourorg.sqlite1j.testkit;

public interface SqlExecutorAdapter {
    String name();

    ExecutionResult executeScript(String script);
}
