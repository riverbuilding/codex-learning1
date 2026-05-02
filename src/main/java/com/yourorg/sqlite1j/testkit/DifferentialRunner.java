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
            differences.add("columns differ: expected=" + ref.columns() + ", actual=" + cand.columns());
        }
        if (!Objects.equals(ref.rows(), cand.rows())) {
            differences.add("rows differ: expected=" + ref.rows() + ", actual=" + cand.rows());
        }
        if (!Objects.equals(ref.errorCategory(), cand.errorCategory())) {
            differences.add("error category differs: expected=" + ref.errorCategory() + ", actual=" + cand.errorCategory());
        }
        if (!Objects.equals(ref.errorMessage(), cand.errorMessage())) {
            differences.add("error message differs: expected=" + ref.errorMessage() + ", actual=" + cand.errorMessage());
        }
        if (!Objects.equals(ref.metadata(), cand.metadata())) {
            differences.add("metadata differs: expected=" + ref.metadata() + ", actual=" + cand.metadata());
        }
        return new DiffResult(differences.isEmpty(), differences);
    }
}
