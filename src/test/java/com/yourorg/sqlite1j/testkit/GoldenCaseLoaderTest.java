package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GoldenCaseLoaderTest {
    @Test
    void loadsDay4Corpus() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        Path corpusPath = resolveCorpusPath("golden/day4");
        List<GoldenCase> cases = loader.load(corpusPath);

        assertEquals(20, cases.size());
        assertEquals("golden_case_01", cases.get(0).name());
        assertEquals("SELECT 1 AS value;", cases.get(0).script());
        assertEquals("1", cases.get(0).expected().rows().get(0).get(0));
    }

    private static Path resolveCorpusPath(String resourceDir) throws URISyntaxException {
        var url = Thread.currentThread().getContextClassLoader().getResource(resourceDir);
        assertNotNull(url, "Missing test resource directory: " + resourceDir);
        return Paths.get(url.toURI());
    }
}
