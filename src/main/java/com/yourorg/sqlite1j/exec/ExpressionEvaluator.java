package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.sql.WhereClause;
import com.yourorg.sqlite1j.types.DbValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ExpressionEvaluator {
    public boolean evaluateWhere(WhereClause where, Map<String, DbValue> row) {
        if (where == null) {
            return true;
        }

        DbValue left = row.get(where.column());
        if (left == null || left.isNull()) {
            return false;
        }

        DbValue right = parseLiteral(where.literal());
        int cmp = compare(left, right);

        switch (where.operator()) {
            case "=":
                return cmp == 0;
            case "!=":
                return cmp != 0;
            case "<":
                return cmp < 0;
            case "<=":
                return cmp <= 0;
            case ">":
                return cmp > 0;
            case ">=":
                return cmp >= 0;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + where.operator());
        }
    }

    public List<DbValue> project(List<String> projections, Map<String, DbValue> row) {
        List<DbValue> out = new ArrayList<>();
        if (projections.size() == 1 && "*".equals(projections.get(0))) {
            out.addAll(row.values());
            return out;
        }

        for (String col : projections) {
            out.add(row.get(col));
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

    private static int compare(DbValue left, DbValue right) {
        if (left.type() == right.type()) {
            switch (left.type()) {
                case INTEGER:
                    return left.asInteger().compareTo(right.asInteger());
                case REAL:
                    return left.asReal().compareTo(right.asReal());
                case TEXT:
                    return left.asText().compareTo(right.asText());
                default:
                    return left.toString().compareTo(right.toString());
            }
        }

        // numeric mixed comparison
        if ((left.type().name().equals("INTEGER") || left.type().name().equals("REAL"))
                && (right.type().name().equals("INTEGER") || right.type().name().equals("REAL"))) {
            double l = left.type().name().equals("INTEGER") ? left.asInteger() : left.asReal();
            double r = right.type().name().equals("INTEGER") ? right.asInteger() : right.asReal();
            return Double.compare(l, r);
        }

        return left.toString().compareTo(right.toString());
    }
}
