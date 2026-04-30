package com.yourorg.sqlite1j.testkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiffAnalyzer {
    public Map<String, Integer> categoryCounts(List<DiffResult> results) {
        Map<String, Integer> counts = new HashMap<>();
        for (DiffResult result : results) {
            if (result.matches()) {
                continue;
            }
            for (String diff : result.differences()) {
                counts.put(diff, counts.getOrDefault(diff, 0) + 1);
            }
        }
        return counts;
    }

    public String topCategory(List<DiffResult> results) {
        Map<String, Integer> counts = categoryCounts(results);
        String best = null;
        int bestCount = -1;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                best = e.getKey();
                bestCount = e.getValue();
            }
        }
        return best;
    }

    public int mismatchCount(List<DiffResult> results) {
        int mismatches = 0;
        for (DiffResult result : results) {
            if (!result.matches()) {
                mismatches++;
            }
        }
        return mismatches;
    }
}
