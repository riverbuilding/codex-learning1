package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldenCorpusDay27Test {
    @Test
    void loadsExpandedDay27Corpus() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        List<GoldenCase> cases = loader.load(resolveCorpusPath("golden/day27"));

        assertEquals(100, cases.size());
        assertEquals("day27_case_001", cases.get(0).name());
        assertEquals("day27_case_100", cases.get(99).name());
    }

    private static Path resolveCorpusPath(String resourceDir) throws Exception {
        var url = Thread.currentThread().getContextClassLoader().getResource(resourceDir);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + resourceDir);
        }
        return Path.of(url.toURI());
    }
}
