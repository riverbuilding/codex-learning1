package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.errors.ErrorParity;
import com.yourorg.sqlite1j.sql.ColumnDef;
import com.yourorg.sqlite1j.sql.CreateTableStatement;
import com.yourorg.sqlite1j.sql.InsertStatement;
import com.yourorg.sqlite1j.sql.SelectStatement;
import com.yourorg.sqlite1j.sql.UpdateStatement;
import com.yourorg.sqlite1j.sql.DeleteStatement;
import com.yourorg.sqlite1j.sql.CreateIndexStatement;
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
    /**
     * Concurrency envelope (minimal parity contract):
     * - all statement/transaction entrypoints are serialized on this database instance
     * - each statement observes a consistent state
     * - rollback restores pre-transaction snapshot atomically relative to other calls
     */
    private final Object mutex = new Object();
    private final Map<String, List<String>> schemas = new HashMap<>();
    private final Map<String, List<Map<String, DbValue>>> rows = new HashMap<>();
    private final Map<String, List<String>> tableIndexes = new HashMap<>();
    private final Map<String, Map<String, List<Integer>>> indexData = new HashMap<>();
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final TransactionManager tx = new TransactionManager();
    private Map<String, List<Map<String, DbValue>>> txSnapshotRows;
    private int lastMutationCount;


    public void beginTransaction() {
        synchronized (mutex) {
            tx.begin();
            txSnapshotRows = deepCopyRows(rows);
        }
    }

    public void commitTransaction() {
        synchronized (mutex) {
            tx.commit();
            txSnapshotRows = null;
        }
    }

    public void rollbackTransaction() {
        synchronized (mutex) {
            tx.rollback();
            if (txSnapshotRows != null) {
                rows.clear();
                rows.putAll(txSnapshotRows);
            }
            txSnapshotRows = null;
        }
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
        if (stmt instanceof CreateIndexStatement) {
            execute((CreateIndexStatement) stmt);
            return List.of();
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
        synchronized (mutex) {
            try {
                return executeStatement(stmt);
            } catch (RuntimeException e) {
                throw new DbException(ErrorParity.normalizeThrowable(e));
            }
        }
    }

    public void execute(CreateTableStatement stmt) {
        List<String> columns = new ArrayList<>();
        for (ColumnDef column : stmt.columns()) {
            columns.add(column.name());
        }
        schemas.put(stmt.tableName(), columns);
        rows.put(stmt.tableName(), new ArrayList<>());
        tableIndexes.put(stmt.tableName(), new ArrayList<String>());
    }

    Map<String, List<String>> exportSchemas() {
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : schemas.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    Map<String, List<Map<String, DbValue>>> exportRows() {
        return deepCopyRows(rows);
    }

    void importState(Map<String, List<String>> importedSchemas, Map<String, List<Map<String, DbValue>>> importedRows) {
        schemas.clear();
        rows.clear();
        tableIndexes.clear();
        indexData.clear();
        for (Map.Entry<String, List<String>> entry : importedSchemas.entrySet()) {
            schemas.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            tableIndexes.put(entry.getKey(), new ArrayList<String>());
        }
        for (Map.Entry<String, List<Map<String, DbValue>>> entry : importedRows.entrySet()) {
            List<Map<String, DbValue>> tableRows = new ArrayList<>();
            for (Map<String, DbValue> row : entry.getValue()) {
                tableRows.add(new LinkedHashMap<>(row));
            }
            rows.put(entry.getKey(), tableRows);
        }
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
        rebuildIndexes(stmt.tableName());
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
        rebuildIndexes(stmt.tableName());
        lastMutationCount = affected;
    }

    public void execute(DeleteStatement stmt) {
        requiredSchema(stmt.tableName());
        List<Map<String, DbValue>> tableRows = rows.get(stmt.tableName());
        int originalSize = tableRows.size();
        tableRows.removeIf(row -> evaluator.evaluateWhere(stmt.whereClause(), row));
        rebuildIndexes(stmt.tableName());
        lastMutationCount = originalSize - tableRows.size();
    }

    public void execute(CreateIndexStatement stmt) {
        List<String> columns = requiredSchema(stmt.tableName());
        if (!columns.contains(stmt.columnName())) {
            throw new IllegalArgumentException("Unknown column '" + stmt.columnName() + "' in table " + stmt.tableName());
        }
        List<String> indexes = tableIndexes.get(stmt.tableName());
        if (!indexes.contains(stmt.columnName())) {
            indexes.add(stmt.columnName());
        }
        rebuildIndex(stmt.tableName(), stmt.columnName());
    }

    public List<List<DbValue>> execute(SelectStatement stmt) {
        ensureSupportedAggregateProjectionMix(stmt.projections(), stmt.groupBy());
        List<Map<String, DbValue>> working = materializeFromItem(stmt.from());
        if (!stmt.from().isSubquery() && stmt.joins().isEmpty() && stmt.whereClause() != null
                && "=".equals(stmt.whereClause().operator())) {
            List<Map<String, DbValue>> indexedRows = lookupByIndex(stmt.from().tableName(), stmt.whereClause().column(), stmt.whereClause().literal());
            if (indexedRows != null) {
                working = indexedRows;
            }
        }
        for (SelectStatement.JoinClause join : stmt.joins()) {
            List<Map<String, DbValue>> rightRows = materializeFromItem(join.right());
            List<Map<String, DbValue>> combined = new ArrayList<>();
            for (Map<String, DbValue> left : working) {
                DbValue lv = resolveColumn(left, join.leftColumn());
                for (Map<String, DbValue> right : rightRows) {
                    DbValue rv = resolveColumn(right, join.rightColumn());
                    if (lv != null && rv != null && lv.toString().equals(rv.toString())) {
                        Map<String, DbValue> m = new LinkedHashMap<>(left);
                        m.putAll(right);
                        combined.add(m);
                    }
                }
            }
            working = combined;
        }

        List<List<DbValue>> out = new ArrayList<>();
        List<RowWithIndex> matched = new ArrayList<>();
        int idx = 0;
        for (Map<String, DbValue> row : working) {
            if (!evaluateWhereScoped(stmt.whereClause(), row)) { idx++; continue; }
            matched.add(new RowWithIndex(row, idx++));
        }
        if (!stmt.groupBy().isEmpty()) {
            List<List<DbValue>> groupedRows = executeGroupedSelect(stmt, matched);
            if (!stmt.orderBy().isEmpty()) {
                // deterministic no-op ordering for grouped output in current scaffold
            }
            int upper = stmt.limit() == null ? groupedRows.size() : Math.min(stmt.limit(), groupedRows.size());
            return new ArrayList<>(groupedRows.subList(0, upper));
        }

        if (!stmt.orderBy().isEmpty()) { matched.sort(rowComparator(stmt.orderBy())); }
        if (containsAggregate(stmt.projections())) { out.add(computeAggregateRow(stmt.projections(), matched)); return out; }
        int upper = stmt.limit() == null ? matched.size() : Math.min(stmt.limit(), matched.size());
        for (int i = 0; i < upper; i++) out.add(projectScoped(stmt.projections(), matched.get(i).row));
        return out;
    }

    private List<Map<String, DbValue>> materializeFromItem(SelectStatement.FromItem item) {
        if (item.isSubquery()) {
            List<List<DbValue>> sub = execute(item.subquery());
            List<Map<String, DbValue>> out = new ArrayList<>();
            for (List<DbValue> row : sub) {
                Map<String, DbValue> map = new LinkedHashMap<>();
                for (int i = 0; i < item.subquery().projections().size(); i++) {
                    String col = item.subquery().projections().get(i);
                    map.put(item.alias() + "." + col, row.get(i));
                }
                out.add(map);
            }
            return out;
        }
        List<String> schema = requiredSchema(item.tableName());
        List<Map<String, DbValue>> out = new ArrayList<>();
        String scope = item.alias() == null ? item.tableName() : item.alias();
        for (Map<String, DbValue> row : rows.get(item.tableName())) {
            Map<String, DbValue> m = new LinkedHashMap<>();
            for (String c : schema) { m.put(scope + "." + c, row.get(c)); }
            out.add(m);
        }
        return out;
    }

    private List<Map<String, DbValue>> lookupByIndex(String tableName, String column, String literal) {
        Map<String, List<Integer>> byColumn = indexData.get(tableName + "." + column);
        if (byColumn == null) {
            return null;
        }
        List<Integer> positions = byColumn.get(parseLiteral(literal).toString());
        if (positions == null) {
            return new ArrayList<Map<String, DbValue>>();
        }
        SelectStatement.FromItem item = SelectStatement.FromItem.table(tableName, null);
        List<String> schema = requiredSchema(tableName);
        List<Map<String, DbValue>> out = new ArrayList<>();
        for (Integer position : positions) {
            Map<String, DbValue> source = rows.get(tableName).get(position.intValue());
            Map<String, DbValue> row = new LinkedHashMap<>();
            for (String c : schema) {
                row.put(tableName + "." + c, source.get(c));
            }
            out.add(row);
        }
        return out;
    }

    private void rebuildIndexes(String tableName) {
        List<String> indexes = tableIndexes.get(tableName);
        if (indexes == null) {
            return;
        }
        for (String indexedColumn : indexes) {
            rebuildIndex(tableName, indexedColumn);
        }
    }

    private void rebuildIndex(String tableName, String columnName) {
        Map<String, List<Integer>> byValue = new HashMap<>();
        List<Map<String, DbValue>> tableRows = rows.get(tableName);
        for (int i = 0; i < tableRows.size(); i++) {
            DbValue value = tableRows.get(i).get(columnName);
            String key = value == null ? "NULL" : value.toString();
            List<Integer> bucket = byValue.get(key);
            if (bucket == null) {
                bucket = new ArrayList<>();
                byValue.put(key, bucket);
            }
            bucket.add(Integer.valueOf(i));
        }
        indexData.put(tableName + "." + columnName, byValue);
    }

    private boolean evaluateWhereScoped(com.yourorg.sqlite1j.sql.WhereClause where, Map<String, DbValue> row) {
        if (where == null) return true;
        DbValue left = resolveColumn(row, where.column());
        DbValue right = parseLiteral(where.literal());
        int cmp = compareForOrder(left, right);
        if ("=".equals(where.operator())) return cmp == 0;
        if ("!=".equals(where.operator())) return cmp != 0;
        if ("<".equals(where.operator())) return cmp < 0;
        if ("<=".equals(where.operator())) return cmp <= 0;
        if (">".equals(where.operator())) return cmp > 0;
        if (">=".equals(where.operator())) return cmp >= 0;
        throw new IllegalArgumentException("Unsupported operator: " + where.operator());
    }

    private List<DbValue> projectScoped(List<String> projections, Map<String, DbValue> row) {
        List<DbValue> out = new ArrayList<>();
        if (projections.size() == 1 && "*".equals(projections.get(0))) {
            java.util.HashSet<String> seen = new java.util.HashSet<>();
            for (Map.Entry<String, DbValue> e : row.entrySet()) {
                String k = e.getKey();
                int dot = k.lastIndexOf('.');
                String base = dot >= 0 ? k.substring(dot + 1) : k;
                if (seen.add(base)) out.add(e.getValue());
            }
            return out;
        }
        for (String p : projections) out.add(resolveColumn(row, p));
        return out;
    }

    private DbValue resolveColumn(Map<String, DbValue> row, String column) {
        if (row.containsKey(column)) return row.get(column);
        DbValue found = null;
        for (Map.Entry<String, DbValue> e : row.entrySet()) {
            if (e.getKey().endsWith("." + column)) {
                if (found != null) throw new IllegalArgumentException("Ambiguous column: " + column);
                found = e.getValue();
            }
        }
        if (found == null) throw new IllegalArgumentException("Unknown column: " + column);
        return found;
    }

    private List<DbValue> computeAggregateRow(List<String> projections, List<RowWithIndex> rows) {
        List<DbValue> out = new ArrayList<>();
        for (String projection : projections) {
            out.add(computeAggregateValue(projection, rows));
        }
        return out;
    }

    private DbValue computeAggregateValue(String projection, List<RowWithIndex> rows) {
        String upper = projection.toUpperCase();
        int open = projection.indexOf('(');
        int close = projection.lastIndexOf(')');
        String arg = projection.substring(open + 1, close).trim();
        if (upper.startsWith("COUNT(")) {
            if ("*".equals(arg)) {
                return DbValue.ofInteger(rows.size());
            }
            long count = 0;
            for (RowWithIndex row : rows) {
                DbValue value = resolveColumn(row.row, arg);
                if (value != null && !value.isNull()) {
                    count++;
                }
            }
            return DbValue.ofInteger(count);
        }
        if (upper.startsWith("MIN(") || upper.startsWith("MAX(")) {
            DbValue best = null;
            for (RowWithIndex row : rows) {
                DbValue value = resolveColumn(row.row, arg);
                if (value == null || value.isNull()) {
                    continue;
                }
                if (best == null) {
                    best = value;
                    continue;
                }
                int cmp = compareForOrder(value, best);
                if ((upper.startsWith("MIN(") && cmp < 0) || (upper.startsWith("MAX(") && cmp > 0)) {
                    best = value;
                }
            }
            return best == null ? DbValue.nullValue() : best;
        }
        throw new IllegalArgumentException("Unsupported aggregate projection: " + projection);
    }

    private boolean containsAggregate(List<String> projections) {
        for (String projection : projections) {
            String upper = projection.toUpperCase();
            if (upper.startsWith("COUNT(") || upper.startsWith("MIN(") || upper.startsWith("MAX(")) {
                return true;
            }
        }
        return false;
    }

    private void ensureSupportedAggregateProjectionMix(List<String> projections, List<String> groupBy) {
        boolean hasAggregate = false;
        boolean hasNonAggregate = false;
        for (String projection : projections) {
            String upper = projection.toUpperCase();
            if (upper.startsWith("COUNT(") || upper.startsWith("MIN(") || upper.startsWith("MAX(")) {
                hasAggregate = true;
            } else {
                hasNonAggregate = true;
            }
        }
        if (hasAggregate && hasNonAggregate && groupBy.isEmpty()) {
            throw new IllegalArgumentException("Unsupported aggregate/non-aggregate projection mix without GROUP BY");
        }
        if (hasAggregate && !groupBy.isEmpty()) {
            for (String projection : projections) {
                String upper = projection.toUpperCase();
                if (!(upper.startsWith("COUNT(") || upper.startsWith("MIN(") || upper.startsWith("MAX("))
                        && !groupBy.contains(projection)) {
                    throw new IllegalArgumentException("Non-aggregate projection must appear in GROUP BY: " + projection);
                }
            }
        }
    }


    private List<List<DbValue>> executeGroupedSelect(SelectStatement stmt, List<RowWithIndex> matched) {
        java.util.LinkedHashMap<String, List<RowWithIndex>> groups = new java.util.LinkedHashMap<>();
        for (RowWithIndex row : matched) {
            StringBuilder key = new StringBuilder();
            for (String g : stmt.groupBy()) {
                DbValue v = resolveColumn(row.row, g);
                key.append(v == null ? "<null>" : v.toString()).append("|");
            }
            groups.computeIfAbsent(key.toString(), k -> new ArrayList<>()).add(row);
        }
        List<List<DbValue>> out = new ArrayList<>();
        for (List<RowWithIndex> grp : groups.values()) {
            List<DbValue> row = new ArrayList<>();
            for (String p : stmt.projections()) {
                String upper = p.toUpperCase();
                if (upper.startsWith("COUNT(") || upper.startsWith("MIN(") || upper.startsWith("MAX(")) {
                    row.add(computeAggregateValue(p, grp));
                } else {
                    row.add(resolveColumn(grp.get(0).row, p));
                }
            }
            if (stmt.havingClause() == null || evaluateHaving(stmt.havingClause(), stmt.projections(), row)) {
                out.add(row);
            }
        }
        return out;
    }

    private boolean evaluateHaving(com.yourorg.sqlite1j.sql.WhereClause having, List<String> projections, List<DbValue> values) {
        int idx = projections.indexOf(having.column());
        if (idx < 0) {
            throw new IllegalArgumentException("HAVING reference must appear in projection: " + having.column());
        }
        DbValue left = values.get(idx);
        DbValue right = parseLiteral(having.literal());
        int cmp = compareForOrder(left, right);
        if ("=".equals(having.operator())) return cmp == 0;
        if ("!=".equals(having.operator())) return cmp != 0;
        if ("<".equals(having.operator())) return cmp < 0;
        if ("<=".equals(having.operator())) return cmp <= 0;
        if (">".equals(having.operator())) return cmp > 0;
        if (">=".equals(having.operator())) return cmp >= 0;
        throw new IllegalArgumentException("Unsupported operator: " + having.operator());
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
