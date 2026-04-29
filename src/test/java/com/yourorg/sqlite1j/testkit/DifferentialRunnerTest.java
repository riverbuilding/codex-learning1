package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DifferentialRunnerTest {
    @Test
    void comparesEquivalentResultsAsMatch() {
        SqlExecutorAdapter left = new StubAdapter("left", new ExecutionResult(
                List.of("a"),
                List.of(List.of("1")),
                null,
                null,
                Map.of("engine", "ref")
        ));
        SqlExecutorAdapter right = new StubAdapter("right", new ExecutionResult(
                List.of("a"),
                List.of(List.of("1")),
                null,
                null,
                Map.of("engine", "ref")
        ));

        DiffResult result = new DifferentialRunner().compare("SELECT 1", left, right);
        assertTrue(result.matches());
    }

    @Test
    void reportsRowDifference() {
        SqlExecutorAdapter left = new StubAdapter("left", new ExecutionResult(
                List.of("a"),
                List.of(List.of("1")),
                null,
                null,
                Map.of()
        ));
        SqlExecutorAdapter right = new StubAdapter("right", new ExecutionResult(
                List.of("a"),
                List.of(List.of("2")),
                null,
                null,
                Map.of()
        ));

        DiffResult result = new DifferentialRunner().compare("SELECT 1", left, right);
        assertFalse(result.matches());
        assertTrue(result.differences().contains("rows differ"));
    }

    private static final class StubAdapter implements SqlExecutorAdapter {
        private final String name;
        private final ExecutionResult result;

        private StubAdapter(String name, ExecutionResult result) {
            this.name = name;
            this.result = result;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public ExecutionResult executeScript(String script) {
            return result;
        }
    }
}
