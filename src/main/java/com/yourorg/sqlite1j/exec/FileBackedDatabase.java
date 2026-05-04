package com.yourorg.sqlite1j.exec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.errors.ErrorNormalizer;
import com.yourorg.sqlite1j.sql.Statement;
import com.yourorg.sqlite1j.types.DbValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileBackedDatabase {
    private static final byte[] SQLITE_MAGIC = "SQLite format 3\u0000".getBytes(StandardCharsets.UTF_8);
    private static final int HEADER_SIZE = 64;
    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();
    private final InMemoryDatabase delegate = new InMemoryDatabase();

    public FileBackedDatabase(Path file) {
        this.file = file;
        load();
    }

    public List<List<DbValue>> executeStatementNormalized(Statement stmt) {
        List<List<DbValue>> result = delegate.executeStatementNormalized(stmt);
        persist();
        return result;
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(file);
            if (bytes.length == 0) {
                return;
            }
            if (bytes.length < HEADER_SIZE) {
                throw new IllegalStateException("corrupt database header");
            }
            for (int i = 0; i < SQLITE_MAGIC.length; i++) {
                if (bytes[i] != SQLITE_MAGIC[i]) {
                    throw new IllegalStateException("unsupported file format");
                }
            }
            byte[] json = new byte[bytes.length - HEADER_SIZE];
            System.arraycopy(bytes, HEADER_SIZE, json, 0, json.length);
            PersistedState state = mapper.readValue(json, new TypeReference<PersistedState>() { });
            delegate.importState(state.schemas, decodeRows(state.rows));
        } catch (IOException ex) {
            throw new DbException(ErrorNormalizer.normalize("STORAGE", "disk i/o while loading database: " + ex.getMessage()));
        } catch (RuntimeException ex) {
            throw new DbException(ErrorNormalizer.normalize("STORAGE", ex.getMessage()));
        }
    }

    private void persist() {
        PersistedState state = new PersistedState();
        state.schemas = delegate.exportSchemas();
        state.rows = encodeRows(delegate.exportRows());
        try {
            byte[] payload = mapper.writeValueAsBytes(state);
            byte[] fileBytes = new byte[HEADER_SIZE + payload.length];
            System.arraycopy(SQLITE_MAGIC, 0, fileBytes, 0, SQLITE_MAGIC.length);
            System.arraycopy(payload, 0, fileBytes, HEADER_SIZE, payload.length);
            Files.write(file, fileBytes);
        } catch (IOException ex) {
            throw new DbException(ErrorNormalizer.normalize("STORAGE", "disk i/o while saving database: " + ex.getMessage()));
        }
    }

    private static Map<String, List<Map<String, EncodedValue>>> encodeRows(Map<String, List<Map<String, DbValue>>> source) {
        Map<String, List<Map<String, EncodedValue>>> out = new HashMap<>();
        for (Map.Entry<String, List<Map<String, DbValue>>> table : source.entrySet()) {
            List<Map<String, EncodedValue>> rows = new java.util.ArrayList<>();
            for (Map<String, DbValue> row : table.getValue()) {
                Map<String, EncodedValue> encodedRow = new HashMap<>();
                for (Map.Entry<String, DbValue> cell : row.entrySet()) {
                    EncodedValue value = new EncodedValue();
                    value.type = cell.getValue().type().name();
                    value.text = valueText(cell.getValue());
                    encodedRow.put(cell.getKey(), value);
                }
                rows.add(encodedRow);
            }
            out.put(table.getKey(), rows);
        }
        return out;
    }

    private static Map<String, List<Map<String, DbValue>>> decodeRows(Map<String, List<Map<String, EncodedValue>>> source) {
        Map<String, List<Map<String, DbValue>>> out = new HashMap<>();
        for (Map.Entry<String, List<Map<String, EncodedValue>>> table : source.entrySet()) {
            List<Map<String, DbValue>> rows = new java.util.ArrayList<>();
            for (Map<String, EncodedValue> row : table.getValue()) {
                Map<String, DbValue> decodedRow = new HashMap<>();
                for (Map.Entry<String, EncodedValue> cell : row.entrySet()) {
                    decodedRow.put(cell.getKey(), decodeValue(cell.getValue()));
                }
                rows.add(decodedRow);
            }
            out.put(table.getKey(), rows);
        }
        return out;
    }

    private static DbValue decodeValue(EncodedValue value) {
        if ("NULL".equals(value.type)) return DbValue.nullValue();
        if ("INTEGER".equals(value.type)) return DbValue.ofInteger(Long.parseLong(value.text));
        if ("REAL".equals(value.type)) return DbValue.ofReal(Double.parseDouble(value.text));
        if ("TEXT".equals(value.type)) return DbValue.ofText(value.text);
        return DbValue.ofBlob(value.text.getBytes(StandardCharsets.UTF_8));
    }

    private static String valueText(DbValue value) {
        switch (value.type()) {
            case NULL:
                return "";
            case INTEGER:
                return String.valueOf(value.asInteger());
            case REAL:
                return String.valueOf(value.asReal());
            case TEXT:
                return value.asText();
            case BLOB:
                return new String(value.asBlob(), StandardCharsets.UTF_8);
            default:
                throw new IllegalArgumentException("Unsupported value type " + value.type());
        }
    }

    static final class PersistedState {
        public Map<String, List<String>> schemas = new HashMap<>();
        public Map<String, List<Map<String, EncodedValue>>> rows = new HashMap<>();
    }

    static final class EncodedValue {
        public String type;
        public String text;
    }
}
