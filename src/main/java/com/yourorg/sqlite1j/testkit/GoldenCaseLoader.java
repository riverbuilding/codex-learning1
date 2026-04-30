package com.yourorg.sqlite1j.testkit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GoldenCaseLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<GoldenCase> load(Path dir) throws IOException {
        List<GoldenCase> out = new ArrayList<>();
        try (var paths = Files.list(dir)) {
            paths.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(path -> out.add(parse(path)));
        }
        return out;
    }

    private GoldenCase parse(Path file) {
        try {
            JsonNode root = MAPPER.readTree(file.toFile());
            String name = requiredText(root, "name", file);
            String script = requiredText(root, "script", file);

            JsonNode expectedNode = required(root, "expected", file);
            List<String> columns = readStringArray(required(expectedNode, "columns", file), "expected.columns", file);
            List<List<String>> rows = readRows(required(expectedNode, "rows", file), file);
            String errorCategory = nullableText(expectedNode.get("errorCategory"));
            String errorMessage = nullableText(expectedNode.get("errorMessage"));
            Map<String, String> metadata = readStringMap(expectedNode.get("metadata"));

            ExecutionResult expected = new ExecutionResult(columns, rows, errorCategory, errorMessage, metadata);
            return new GoldenCase(name, script, expected);
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading golden case: " + file, e);
        }
    }

    private static JsonNode required(JsonNode node, String field, Path file) {
        JsonNode child = node.get(field);
        if (child == null || child.isMissingNode() || child.isNull()) {
            throw new IllegalArgumentException("Missing field " + field + " in " + file);
        }
        return child;
    }

    private static String requiredText(JsonNode node, String field, Path file) {
        JsonNode child = required(node, field, file);
        if (!child.isTextual()) {
            throw new IllegalArgumentException("Field " + field + " must be text in " + file);
        }
        return child.asText();
    }

    private static String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private static List<String> readStringArray(JsonNode array, String field, Path file) {
        if (!array.isArray()) {
            throw new IllegalArgumentException("Field " + field + " must be array in " + file);
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : array) {
            out.add(item.asText());
        }
        return out;
    }

    private static List<List<String>> readRows(JsonNode rowsNode, Path file) {
        if (!rowsNode.isArray()) {
            throw new IllegalArgumentException("Field expected.rows must be array in " + file);
        }
        List<List<String>> rows = new ArrayList<>();
        for (JsonNode rowNode : rowsNode) {
            if (!rowNode.isArray()) {
                throw new IllegalArgumentException("Each row must be an array in " + file);
            }
            List<String> row = new ArrayList<>();
            for (JsonNode cell : rowNode) {
                row.add(cell.isNull() ? null : cell.asText());
            }
            rows.add(row);
        }
        return rows;
    }

    private static Map<String, String> readStringMap(JsonNode metadataNode) {
        Map<String, String> out = new HashMap<>();
        if (metadataNode == null || metadataNode.isNull()) {
            return out;
        }
        metadataNode.fields().forEachRemaining(entry -> out.put(entry.getKey(), entry.getValue().asText()));
        return out;
    }
}
