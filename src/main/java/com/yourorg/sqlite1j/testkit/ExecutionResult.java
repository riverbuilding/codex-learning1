package com.yourorg.sqlite1j.testkit;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ExecutionResult {
    private final List<String> columns;
    private final List<List<String>> rows;
    private final String errorCategory;
    private final String errorMessage;
    private final Map<String, String> metadata;

    public ExecutionResult(List<String> columns,
                           List<List<String>> rows,
                           String errorCategory,
                           String errorMessage,
                           Map<String, String> metadata) {
        this.columns = columns;
        this.rows = rows;
        this.errorCategory = errorCategory;
        this.errorMessage = errorMessage;
        this.metadata = metadata;
    }

    public List<String> columns() { return columns; }
    public List<List<String>> rows() { return rows; }
    public String errorCategory() { return errorCategory; }
    public String errorMessage() { return errorMessage; }
    public Map<String, String> metadata() { return metadata; }

    public boolean hasError() {
        return errorCategory != null && !errorCategory.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionResult)) return false;
        ExecutionResult that = (ExecutionResult) o;
        return Objects.equals(columns, that.columns)
                && Objects.equals(rows, that.rows)
                && Objects.equals(errorCategory, that.errorCategory)
                && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columns, rows, errorCategory, errorMessage, metadata);
    }
}
