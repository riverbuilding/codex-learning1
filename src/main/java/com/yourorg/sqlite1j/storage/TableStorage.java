package com.yourorg.sqlite1j.storage;

import com.yourorg.sqlite1j.exec.ExpressionEvaluator;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.types.DbValue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TableStorage {
    private final String tableName;
    private final List<String> columns;
    private final BTree rows;
    private long nextRowId = 1;
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    public TableStorage(String tableName, List<String> columns) {
        this.tableName = tableName;
        this.columns = new ArrayList<>(columns);
        this.rows = new BTree();
    }

    public long insert(List<DbValue> values) {
        if (values.size() != columns.size()) {
            throw new IllegalArgumentException("Arity mismatch for table " + tableName);
        }
        byte[] encoded = encode(values);
        long rowId = nextRowId++;
        rows.insert(rowId, encoded);
        return rowId;
    }

    public List<Map<String, DbValue>> scanAll() {
        List<Map<String, DbValue>> out = new ArrayList<>();
        for (Long key : rows.orderedKeys()) {
            byte[] encoded = rows.search(key);
            List<DbValue> decoded = decode(encoded);
            Map<String, DbValue> row = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                row.put(columns.get(i), decoded.get(i));
            }
            out.add(row);
        }
        return out;
    }

    public boolean updateByRowId(long rowId, List<DbValue> values) {
        if (values.size() != columns.size()) {
            throw new IllegalArgumentException("Arity mismatch for table " + tableName);
        }
        if (rows.search(rowId) == null) {
            return false;
        }
        rows.insert(rowId, encode(values));
        return true;
    }

    public boolean deleteByRowId(long rowId) {
        return rows.delete(rowId);
    }


    public List<List<DbValue>> select(SelectStatement stmt) {
        List<List<DbValue>> out = new ArrayList<>();
        for (Map<String, DbValue> row : scanAll()) {
            if (!evaluator.evaluateWhere(stmt.whereClause(), row)) {
                continue;
            }
            out.add(evaluator.project(stmt.projections(), row));
        }
        return out;
    }

    private static byte[] encode(List<DbValue> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            DbValue v = values.get(i);
            sb.append(v.type().name()).append(':').append(toText(v));
            if (i < values.size() - 1) sb.append('|');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static List<DbValue> decode(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        String[] parts = s.split("\\|", -1);
        List<DbValue> out = new ArrayList<>();
        for (String part : parts) {
            int idx = part.indexOf(':');
            String type = idx >= 0 ? part.substring(0, idx) : "TEXT";
            String val = idx >= 0 ? part.substring(idx + 1) : part;
            out.add(fromText(type, val));
        }
        return out;
    }

    private static String toText(DbValue v) {
        switch (v.type()) {
            case NULL: return "";
            case INTEGER: return String.valueOf(v.asInteger());
            case REAL: return String.valueOf(v.asReal());
            case TEXT: return v.asText();
            case BLOB: return new String(v.asBlob(), StandardCharsets.UTF_8);
            default: throw new IllegalArgumentException("Unsupported type " + v.type());
        }
    }

    private static DbValue fromText(String type, String value) {
        switch (type) {
            case "NULL": return DbValue.nullValue();
            case "INTEGER": return DbValue.ofInteger(Long.parseLong(value));
            case "REAL": return DbValue.ofReal(Double.parseDouble(value));
            case "TEXT": return DbValue.ofText(value);
            case "BLOB": return DbValue.ofBlob(value.getBytes(StandardCharsets.UTF_8));
            default: return DbValue.ofText(value);
        }
    }
}
