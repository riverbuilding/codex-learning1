package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.errors.ErrorParity;
import com.yourorg.sqlite1j.sql.ColumnDef;
import com.yourorg.sqlite1j.sql.CreateTableStatement;
import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.sql.Statement;
import com.yourorg.sqlite1j.sql.TransactionCommand;
import com.yourorg.sqlite1j.sql.TransactionStatement;
import com.yourorg.sqlite1j.txn.TransactionManager;
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
    private final TransactionManager tx = new TransactionManager();
    private Map<String, List<Map<String, DbValue>>> txSnapshotRows;


    public void beginTransaction() {
        tx.begin();
        txSnapshotRows = deepCopyRows(rows);
    }

    public void commitTransaction() {
        tx.commit();
        txSnapshotRows = null;
    }

    public void rollbackTransaction() {
        tx.rollback();
        if (txSnapshotRows != null) {
            rows.clear();
            rows.putAll(txSnapshotRows);
        }
        txSnapshotRows = null;
    }


    private List<List<DbValue>> executeStatement(Statement stmt) {
        if (stmt instanceof CreateTableStatement) {
            execute((CreateTableStatement) stmt);
            return List.of();
        }
        if (stmt instanceof InsertStatement) {
            execute((InsertStatement) stmt);
            return List.of();
        }
        if (stmt instanceof SelectStatement) {
            return execute((SelectStatement) stmt);
        }
        if (stmt instanceof TransactionStatement) {
            TransactionCommand command = ((TransactionStatement) stmt).command();
            switch (command) {
                case BEGIN:
                    beginTransaction();
                    break;
                case COMMIT:
                    commitTransaction();
                    break;
                case ROLLBACK:
                    rollbackTransaction();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported transaction command: " + command);
            }
            return List.of();
        }

        throw new IllegalArgumentException("Unsupported statement type: " + stmt.getClass().getName());
    }

    public List<List<DbValue>> executeStatementNormalized(Statement stmt) {
        try {
            return executeStatement(stmt);
        } catch (RuntimeException e) {
            throw new DbException(ErrorParity.normalizeThrowable(e));
        }
    }

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


    private static Map<String, List<Map<String, DbValue>>> deepCopyRows(Map<String, List<Map<String, DbValue>>> source) {
        Map<String, List<Map<String, DbValue>>> out = new HashMap<>();
        for (Map.Entry<String, List<Map<String, DbValue>>> e : source.entrySet()) {
            List<Map<String, DbValue>> copiedRows = new ArrayList<>();
            for (Map<String, DbValue> row : e.getValue()) {
                copiedRows.add(new LinkedHashMap<>(row));
            }
            out.put(e.getKey(), copiedRows);
        }
        return out;
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
