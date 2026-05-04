package com.yourorg.sqlite1j;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParityMatrixCoverageTest {
    @Test
    void allParityMatrixRowsMapToExecutableTestIds() throws Exception {
        String matrix = Files.readString(Path.of("PARITY_MATRIX.md"));
        List<String> ids = extractTestIds(matrix);
        assertFalse(ids.isEmpty());

        for (String id : ids) {
            String[] parts = id.split("#");
            assertTrue(parts.length == 2, "Malformed test id: " + id);
            String className = parts[0];
            String methodName = parts[1];
            Path classFile = findTestClassFile(className);
            assertTrue(classFile != null, "Missing class file for " + id);
            String source = Files.readString(classFile);
            assertTrue(source.contains(methodName + "()"), "Missing method for " + id);
        }
    }

    private static List<String> extractTestIds(String matrix) {
        List<String> ids = new ArrayList<>();
        for (String line : matrix.split("\n")) {
            if (!line.startsWith("| P-")) continue;
            int lastPipe = line.lastIndexOf('|');
            int prevPipe = line.lastIndexOf('|', lastPipe - 1);
            if (prevPipe < 0 || lastPipe < 0) continue;
            String cell = line.substring(prevPipe + 1, lastPipe).trim();
            for (String piece : cell.split(",")) {
                ids.add(piece.trim().replace("`", ""));
            }
        }
        return ids;
    }

    private static Path findTestClassFile(String simpleClassName) throws Exception {
        try (var paths = Files.walk(Path.of("src/test/java"))) {
            return paths
                    .filter(p -> p.getFileName().toString().equals(simpleClassName + ".java"))
                    .findFirst()
                    .orElse(null);
        }
    }
}
