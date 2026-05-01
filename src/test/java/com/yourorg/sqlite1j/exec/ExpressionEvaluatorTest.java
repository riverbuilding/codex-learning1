package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.sql.WhereClause;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionEvaluatorTest {
    @Test
    void evaluatesWhereComparisons() {
        ExpressionEvaluator ev = new ExpressionEvaluator();
        Map<String, DbValue> row = Map.of(
                "id", DbValue.ofInteger(7),
                "name", DbValue.ofText("alice")
        );

        assertTrue(ev.evaluateWhere(new WhereClause("id", "=", "7"), row));
        assertTrue(ev.evaluateWhere(new WhereClause("id", ">", "6"), row));
        assertTrue(ev.evaluateWhere(new WhereClause("id", "<", "8"), row));
        assertTrue(ev.evaluateWhere(new WhereClause("id", "!=", "8"), row));
        assertTrue(ev.evaluateWhere(new WhereClause("id", "<=", "7"), row));
        assertTrue(ev.evaluateWhere(new WhereClause("id", ">=", "7"), row));
        assertFalse(ev.evaluateWhere(new WhereClause("name", "=", "bob"), row));
    }

    @Test
    void projectsRequestedColumnsAndStar() {
        ExpressionEvaluator ev = new ExpressionEvaluator();
        Map<String, DbValue> row = new LinkedHashMap<>();
        row.put("id", DbValue.ofInteger(1));
        row.put("name", DbValue.ofText("alice"));

        List<DbValue> projected = ev.project(List.of("name"), row);
        assertEquals(1, projected.size());
        assertEquals("alice", projected.get(0).asText());

        List<DbValue> star = ev.project(List.of("*"), row);
        assertEquals(2, star.size());
    }
}
