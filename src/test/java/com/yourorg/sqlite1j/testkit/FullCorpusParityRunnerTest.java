package com.yourorg.sqlite1j.testkit;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.exec.InMemoryDatabase;
import com.yourorg.sqlite1j.exec.SqlCommandRunner;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

class FullCorpusParityRunnerTest {
    @Test
    void fullCorpusParityReportIsDeterministicAndAggregated() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("fullCorpusParity"),
                "Set -DfullCorpusParity=true to execute full-corpus parity audit.");
        List<GoldenCase> allCases = loadAllCases();
        List<String> mismatches = new ArrayList<>();
        CandidateExecutor candidate = new CandidateExecutor();

        for (GoldenCase goldenCase : allCases) {
            ExecutionResult actual = candidate.execute(goldenCase.script());
            mismatches.addAll(compare(goldenCase, actual));
        }

        if (!mismatches.isEmpty()) {
            StringBuilder summary = new StringBuilder();
            summary.append("Full-corpus parity mismatches: ").append(mismatches.size()).append('\n');
            int limit = Math.min(40, mismatches.size());
            for (int i = 0; i < limit; i++) {
                summary.append(" - ").append(mismatches.get(i)).append('\n');
            }
            if (mismatches.size() > limit) {
                summary.append(" ... ").append(mismatches.size() - limit).append(" more");
            }
            fail(summary.toString());
        }
    }

    private static List<GoldenCase> loadAllCases() throws Exception {
        GoldenCaseLoader loader = new GoldenCaseLoader();
        Path root = resolveCorpusPath("golden");
        List<Path> dayDirs = new ArrayList<>();
        try (var paths = Files.list(root)) {
            paths.filter(Files::isDirectory).forEach(dayDirs::add);
        }
        Collections.sort(dayDirs);

        List<GoldenCase> all = new ArrayList<>();
        for (Path dayDir : dayDirs) {
            all.addAll(loader.load(dayDir));
        }
        return all;
    }

    private static List<String> compare(GoldenCase goldenCase, ExecutionResult actual) {
        List<String> diffs = new ArrayList<>();
        if (!goldenCase.expected().columns().equals(actual.columns())) {
            diffs.add(goldenCase.name() + " columns expected=" + goldenCase.expected().columns() + " actual=" + actual.columns());
        }
        if (!goldenCase.expected().rows().equals(actual.rows())) {
            diffs.add(goldenCase.name() + " rows expected=" + goldenCase.expected().rows() + " actual=" + actual.rows());
        }
        if (!equalsNullable(goldenCase.expected().errorCategory(), actual.errorCategory())) {
            diffs.add(goldenCase.name() + " errorCategory expected=" + goldenCase.expected().errorCategory() + " actual=" + actual.errorCategory());
        }
        return diffs;
    }

    private static boolean equalsNullable(String left, String right) {
        if (left == null) return right == null;
        return left.equals(right);
    }

    private static Path resolveCorpusPath(String resourceDir) throws Exception {
        var url = Thread.currentThread().getContextClassLoader().getResource(resourceDir);
        if (url == null) {
            throw new IllegalStateException("Missing test resource directory: " + resourceDir);
        }
        return Path.of(url.toURI());
    }

    private static final class CandidateExecutor {
        private final Parser parser = new Parser();

        ExecutionResult execute(String script) {
            SqlCommandRunner runner = new SqlCommandRunner(parser, new InMemoryDatabase());
            List<List<DbValue>> lastRows = List.of();
            List<String> lastColumns = List.of();
            try {
                for (String statement : splitStatements(script)) {
                    String sql = statement.trim();
                    if (sql.isEmpty()) {
                        continue;
                    }
                    lastRows = runner.execute(sql);
                    if (sql.toUpperCase().startsWith("SELECT")) {
                        SelectStatement select = parser.parseSelect(sql);
                        lastColumns = new ArrayList<>(select.projections());
                    }
                }
                return new ExecutionResult(lastColumns, rowsToText(lastRows), null, null, java.util.Map.of());
            } catch (DbException ex) {
                return new ExecutionResult(lastColumns, rowsToText(lastRows), ex.error().category().name(), ex.error().message(), java.util.Map.of());
            }
        }

        private static List<String> splitStatements(String script) {
            List<String> out = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean inString = false;
            for (int i = 0; i < script.length(); i++) {
                char c = script.charAt(i);
                if (c == '\'') {
                    inString = !inString;
                }
                if (c == ';' && !inString) {
                    out.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            if (current.length() > 0) {
                out.add(current.toString());
            }
            return out;
        }

        private static List<List<String>> rowsToText(List<List<DbValue>> rows) {
            List<List<String>> out = new ArrayList<>();
            for (List<DbValue> row : rows) {
                List<String> converted = new ArrayList<>();
                for (DbValue value : row) {
                    converted.add(asText(value));
                }
                out.add(converted);
            }
            return out;
        }

        private static String asText(DbValue value) {
            if (value == null || value.isNull()) return null;
            switch (value.type()) {
                case INTEGER:
                    return String.valueOf(value.asInteger());
                case REAL:
                    return String.valueOf(value.asReal());
                case TEXT:
                    return value.asText();
                case BLOB:
                    return new String(value.asBlob());
                default:
                    return null;
            }
        }
    }
}
