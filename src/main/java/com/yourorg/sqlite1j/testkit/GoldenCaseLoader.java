package com.yourorg.sqlite1j.testkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GoldenCaseLoader {
    private static final Pattern NAME_PATTERN = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("\\\"script\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern VALUE_PATTERN = Pattern.compile("\\\"rows\\\"\\s*:\\s*\\[\\s*\\[\\s*\\\"([^\\\"]+)\\\"\\s*]]");

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
            String json = Files.readString(file, StandardCharsets.UTF_8);
            String name = group(NAME_PATTERN, json, "name", file);
            String script = group(SCRIPT_PATTERN, json, "script", file);
            String value = group(VALUE_PATTERN, json, "rows[0][0]", file);
            ExecutionResult expected = new ExecutionResult(
                    List.of("value"),
                    List.of(List.of(value)),
                    null,
                    null,
                    Map.of("source", file.getFileName().toString())
            );
            return new GoldenCase(name, script, expected);
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading golden case: " + file, e);
        }
    }

    private static String group(Pattern pattern, String input, String field, Path file) {
        Matcher m = pattern.matcher(input);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing field " + field + " in " + file);
        }
        return m.group(1);
    }
}
