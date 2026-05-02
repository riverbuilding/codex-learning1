package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.errors.ErrorParity;
import com.yourorg.sqlite1j.sql.ColumnDef;
import com.yourorg.sqlite1j.sql.CreateTableStatement;
import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.sql.UpdateStatement;
import com.yourorg.sqlite1j.sql.DeleteStatement;
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
import java.util.Comparator;

public final class InMemoryDatabase {
    private final Map<String, List<String>> schemas = new HashMap<>();
    private final Map<String, List<Map<String, DbValue>>> rows = new HashMap<>();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final TransactionManager tx = new TransactionManager();
    private Map<String, List<Map<String, DbValue>>> txSnapshotRows;
    private int lastMutationCount;


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
        if (stmt instanceof UpdateStatement) {
            execute((UpdateStatement) stmt);
            return List.of();
        }
        if (stmt instanceof DeleteStatement) {
            execute((DeleteStatement) stmt);
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
        lastMutationCount = 1;
    }

    public void execute(UpdateStatement stmt) {
        List<String> columns = requiredSchema(stmt.tableName());
        int affected = 0;
        for (Map<String, DbValue> row : rows.get(stmt.tableName())) {
            if (!evaluator.evaluateWhere(stmt.whereClause(), row)) {
                continue;
            }
            for (UpdateStatement.Assignment assignment : stmt.assignments()) {
                if (!columns.contains(assignment.column())) {
                    throw new IllegalArgumentException("Unknown column '" + assignment.column() + "' in table " + stmt.tableName());
                }
                row.put(assignment.column(), parseLiteral(assignment.literal()));
            }
            affected++;
        }
        lastMutationCount = affected;
    }

    public void execute(DeleteStatement stmt) {
        requiredSchema(stmt.tableName());
        List<Map<String, DbValue>> tableRows = rows.get(stmt.tableName());
        int originalSize = tableRows.size();
        tableRows.removeIf(row -> evaluator.evaluateWhere(stmt.whereClause(), row));
        lastMutationCount = originalSize - tableRows.size();
    }

    public List<List<DbValue>> execute(SelectStatement stmt) {
        requiredSchema(stmt.fromTable());
        List<List<DbValue>> out = new ArrayList<>();
        List<RowWithIndex> matched = new ArrayList<>();
        int idx = 0;

        for (Map<String, DbValue> row : rows.get(stmt.fromTable())) {
            if (!evaluator.evaluateWhere(stmt.whereClause(), row)) {
                idx++;
                continue;
            }
            matched.add(new RowWithIndex(row, idx));
            idx++;
        }

        if (!stmt.orderBy().isEmpty()) {
            matched.sort(rowComparator(stmt.orderBy()));
        }

        Integer limit = stmt.limit();
        int upper = limit == null ? matched.size() : Math.min(limit, matched.size());
        for (int i = 0; i < upper; i++) {
            out.add(evaluator.project(stmt.projections(), matched.get(i).row));
        }
        return out;
    }

    private Comparator<RowWithIndex> rowComparator(List<SelectStatement.OrderByTerm> terms) {
        return (a, b) -> {
            for (SelectStatement.OrderByTerm term : terms) {
                DbValue left = a.row.get(term.column());
                DbValue right = b.row.get(term.column());
                int cmp = compareForOrder(left, right);
                if (cmp != 0) {
                    return term.ascending() ? cmp : -cmp;
                }
            }
            return Integer.compare(a.index, b.index);
        };
    }

    private int compareForOrder(DbValue left, DbValue right) {
        if (left == null || left.isNull()) {
            return (right == null || right.isNull()) ? 0 : 1; // NULLS LAST
        }
        if (right == null || right.isNull()) {
            return -1;
        }
        String l = left.toString();
        String r = right.toString();
        return l.compareTo(r);
    }

    private static final class RowWithIndex {
        private final Map<String, DbValue> row;
        private final int index;

        private RowWithIndex(Map<String, DbValue> row, int index) {
            this.row = row;
            this.index = index;
        }
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

    public int lastMutationCount() {
        return lastMutationCount;
    }
}
