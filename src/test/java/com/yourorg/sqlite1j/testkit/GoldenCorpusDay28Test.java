package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldenCorpusDay28Test {
    @Test
    void loadsDifferentialHarnessExpansionCorpus() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        List<GoldenCase> cases = loader.load(resolveCorpusPath("golden/day28"));

        assertEquals(6, cases.size());
        assertEquals("day28_case_001_not_equal_operator_true", cases.get(0).name());
        assertEquals("day28_case_006_schema_error_parity", cases.get(5).name());
    }

    private static Path resolveCorpusPath(String resourceDir) throws Exception {
        var url = Thread.currentThread().getContextClassLoader().getResource(resourceDir);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + resourceDir);
        }
        return Path.of(url.toURI());
    }
}
