package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldenCorpusDay35Test {
    @Test
    void loadsPhase3FeatureCorpus() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        List<GoldenCase> cases = loader.load(resolveCorpusPath("golden/day35"));

        assertEquals(7, cases.size());
        assertEquals("day35_case_001_update_where", cases.get(0).name());
        assertEquals("day35_case_007_aggregate_mix_unsupported", cases.get(6).name());
    }

    private static Path resolveCorpusPath(String resourceDir) throws Exception {
        var url = Thread.currentThread().getContextClassLoader().getResource(resourceDir);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + resourceDir);
        }
        return Path.of(url.toURI());
    }
}
