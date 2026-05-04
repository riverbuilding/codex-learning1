package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldenCorpusDay36Test {
    @Test
    void loadsParityExpansionCorpusForIndexedAndTxnCases() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        List<GoldenCase> cases = loader.load(resolveCorpusPath("golden/day36"));

        assertEquals(4, cases.size());
        assertEquals("day36_case_001_create_index_and_lookup", cases.get(0).name());
        assertEquals("day36_case_004_txn_error_commit_without_begin", cases.get(3).name());
    }

    private static Path resolveCorpusPath(String resourceDir) throws Exception {
        var url = Thread.currentThread().getContextClassLoader().getResource(resourceDir);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + resourceDir);
        }
        return Path.of(url.toURI());
    }
}
