package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiffAnalyzerTest {
    @Test
    void computesMismatchCountsAndTopCategory() {
        List<DiffResult> results = List.of(
                new DiffResult(true, List.of()),
                new DiffResult(false, List.of("rows differ")),
                new DiffResult(false, List.of("rows differ", "metadata differs")),
                new DiffResult(false, List.of("columns differ"))
        );

        DiffAnalyzer analyzer = new DiffAnalyzer();
        Map<String, Integer> counts = analyzer.categoryCounts(results);

        assertEquals(3, analyzer.mismatchCount(results));
        assertEquals(2, counts.get("rows differ"));
        assertEquals(1, counts.get("metadata differs"));
        assertEquals("rows differ", analyzer.topCategory(results));
    }
}
