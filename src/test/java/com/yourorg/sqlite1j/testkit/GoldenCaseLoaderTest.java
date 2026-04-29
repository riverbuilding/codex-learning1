package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldenCaseLoaderTest {
    @Test
    void loadsDay4Corpus() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        List<GoldenCase> cases = loader.load(Path.of("src/test/resources/golden/day4"));

        assertEquals(20, cases.size());
        assertEquals("golden_case_01", cases.get(0).name());
        assertEquals("SELECT 1 AS value;", cases.get(0).script());
        assertEquals("1", cases.get(0).expected().rows().get(0).get(0));
    }
}
