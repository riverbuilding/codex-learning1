package com.yourorg.sqlite1j.testkit;

import java.util.List;
import java.util.Map;

public record ExecutionResult(
        List<String> columns,
        List<List<String>> rows,
        String errorCategory,
        String errorMessage,
        Map<String, String> metadata
) {
    public boolean hasError() {
        return errorCategory != null && !errorCategory.isBlank();
    }
}
