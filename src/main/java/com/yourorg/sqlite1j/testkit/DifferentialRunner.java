package com.yourorg.sqlite1j.testkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DifferentialRunner {
    public DiffResult compare(String script, SqlExecutorAdapter reference, SqlExecutorAdapter candidate) {
        ExecutionResult ref = reference.executeScript(script);
        ExecutionResult cand = candidate.executeScript(script);

        List<String> differences = new ArrayList<>();
        if (!Objects.equals(ref.columns(), cand.columns())) {
            differences.add("columns differ");
        }
        if (!Objects.equals(ref.rows(), cand.rows())) {
            differences.add("rows differ");
        }
        if (!Objects.equals(ref.errorCategory(), cand.errorCategory())) {
            differences.add("error category differs");
        }
        if (!Objects.equals(ref.errorMessage(), cand.errorMessage())) {
            differences.add("error message differs");
        }
        if (!Objects.equals(ref.metadata(), cand.metadata())) {
            differences.add("metadata differs");
        }
        return new DiffResult(differences.isEmpty(), differences);
    }
}
