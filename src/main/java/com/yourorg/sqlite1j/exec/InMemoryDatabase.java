package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.sql.ColumnDef;
import com.yourorg.sqlite1j.sql.CreateTableStatement;
import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.types.DbValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryDatabase {
    private final Map<String, List<String>> schemas = new HashMap<>();
    private final Map<String, List<Map<String, DbValue>>> rows = new HashMap<>();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    public void execute(CreateTableStatement stmt) {
        List<String> columns = new ArrayList<>();
        for (ColumnDef column : stmt.columns()) {
            columns.add(column.name());
        }
        schemas.put(stmt.tableName(), columns);
        rows.put(stmt.tableName(), new ArrayList<>());
    }

    public void execute(InsertStatement stmt) {
        List<String> columns = requiredSchema(stmt.tableName());
        if (columns.size() != stmt.values().size()) {
            throw new IllegalArgumentException("Value count does not match column count for table " + stmt.tableName());
        }

        Map<String, DbValue> row = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            row.put(columns.get(i), parseLiteral(stmt.values().get(i)));
        }
        rows.get(stmt.tableName()).add(row);
    }

    public List<List<DbValue>> execute(SelectStatement stmt) {
        requiredSchema(stmt.fromTable());
        List<List<DbValue>> out = new ArrayList<>();

        for (Map<String, DbValue> row : rows.get(stmt.fromTable())) {
            if (!evaluator.evaluateWhere(stmt.whereClause(), row)) {
                continue;
            }
            out.add(evaluator.project(stmt.projections(), row));
        }
        return out;
    }

    private List<String> requiredSchema(String tableName) {
        List<String> schema = schemas.get(tableName);
        if (schema == null) {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }
        return schema;
    }

    private static DbValue parseLiteral(String literal) {
        try {
            if (literal.contains(".")) {
                return DbValue.ofReal(Double.parseDouble(literal));
            }
            return DbValue.ofInteger(Long.parseLong(literal));
        } catch (NumberFormatException ignore) {
            return DbValue.ofText(literal);
        }
    }
}
