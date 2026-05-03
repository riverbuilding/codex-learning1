package com.yourorg.sqlite1j.testkit;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoldenCorpusDiscoveryTest {
    @Test
    void discoversAndLoadsAllGoldenDayFolders() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        Path goldenRoot = resolveCorpusPath("golden");

        try (var directories = Files.list(goldenRoot)) {
            List<Path> dayDirs = directories
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("day"))
                    .sorted()
                    .collect(Collectors.toList());

            assertFalse(dayDirs.isEmpty(), "Expected at least one day* corpus folder under golden/");
            for (Path dayDir : dayDirs) {
                List<GoldenCase> cases = loader.load(dayDir);
                assertFalse(cases.isEmpty(), "Expected non-empty corpus for " + dayDir.getFileName());
            }

            assertTrue(dayDirs.stream().anyMatch(path -> path.getFileName().toString().equals("day28")),
                    "Expected day28 corpus folder to be discoverable");
        }
    }

    private static Path resolveCorpusPath(String resourceDir) throws Exception {
        var url = Thread.currentThread().getContextClassLoader().getResource(resourceDir);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + resourceDir);
        }
        return Path.of(URI.create(url.toString()));
    }
}
