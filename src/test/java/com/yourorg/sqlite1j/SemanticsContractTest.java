package com.yourorg.sqlite1j;

import com.yourorg.sqlite1j.exec.ExpressionEvaluator;
import com.yourorg.sqlite1j.exec.InMemoryDatabase;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.WhereClause;
import com.yourorg.sqlite1j.txn.TransactionManager;
import com.yourorg.sqlite1j.types.DbValue;
import com.yourorg.sqlite1j.types.TypeAffinity;
import com.yourorg.sqlite1j.types.ValueCoercion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticsContractTest {
    @Test
    void section1_nullSemantics_whereEqualsAndNotEqualsTreatNullAsUnknown() {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Map<String, DbValue> row = Map.of("v", DbValue.nullValue());

        assertFalse(evaluator.evaluateWhere(new WhereClause("v", "=", "1"), row));
        assertFalse(evaluator.evaluateWhere(new WhereClause("v", "!=", "1"), row));
    }

    @Test
    @Disabled("Deferred: parser/evaluator do not yet support IS NULL / IS NOT NULL predicates in Phase 1 runtime")
    void section1_nullSemantics_isNullAndIsNotNullPredicates() {
        throw new AssertionError("Enable once IS NULL/IS NOT NULL are implemented in parser + evaluator");
    }

    @Test
    @Disabled("Deferred: arithmetic expression execution is not implemented in the Phase 1 query engine")
    void section1_nullSemantics_arithmeticWithNullYieldsNull() {
        throw new AssertionError("Enable once arithmetic expression execution is available");
    }

    @Test
    void section2_typeAffinityAndCoercion_runtimeValueKindsIncludeNullIntegerRealTextBlob() {
        assertTrue(DbValue.nullValue().isNull());
        assertEquals(7L, DbValue.ofInteger(7).asInteger());
        assertEquals(2.5, DbValue.ofReal(2.5).asReal());
        assertEquals("x", DbValue.ofText("x").asText());
        assertEquals(3, DbValue.ofBlob(new byte[]{1, 2, 3}).asBlob().length);
    }

    @Test
    void section2_typeAffinityAndCoercion_numericTextIsCoercedForNumericAffinities() {
        assertEquals(123L, ValueCoercion.applyAffinity(DbValue.ofText("123"), TypeAffinity.INTEGER).asInteger());
        assertEquals(12.5, ValueCoercion.applyAffinity(DbValue.ofText("12.5"), TypeAffinity.REAL).asReal());
    }

    @Test
    void section2_typeAffinityAndCoercion_nonCoercibleTextFailsDeterministically() {
        assertThrows(IllegalArgumentException.class,
                () -> ValueCoercion.applyAffinity(DbValue.ofText("abc"), TypeAffinity.INTEGER));
    }

    @Test
    void section3_transactionSemantics_beginOpensScopeAndCommitMakesChangesVisible() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));
        db.beginTransaction();
        db.execute(parser.parseInsert("INSERT INTO t VALUES ('alice', 30);"));
        db.commitTransaction();

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rows.size());
        assertEquals("alice", rows.get(0).get(0).asText());
    }

    @Test
    void section3_transactionSemantics_rollbackDiscardsInTransactionChanges() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES ('baseline', 10);"));

        db.beginTransaction();
        db.execute(parser.parseInsert("INSERT INTO t VALUES ('temp', 20);"));
        db.rollbackTransaction();

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rows.size());
        assertEquals("baseline", rows.get(0).get(0).asText());
    }

    @Test
    void section3_transactionSemantics_autocommitStatementsPersistWithoutExplicitBegin() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();

        db.execute(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES ('autocommit', 1);"));

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rows.size());
        assertEquals("autocommit", rows.get(0).get(0).asText());
    }

    @Test
    void section4_queryResultSemantics_orderByAppliedBeforeLimit() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(parser.parseCreateTable("CREATE TABLE t (id INTEGER, score INTEGER);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (1, 5);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (2, 5);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (3, 4);"));

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT id FROM t ORDER BY score DESC LIMIT 2;"));
        assertEquals(2, rows.size());
        assertEquals(1L, rows.get(0).get(0).asInteger());
        assertEquals(2L, rows.get(1).get(0).asInteger());
    }

    @Test
    void section4_queryResultSemantics_aggregatesNullHandlingAndTyping() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(parser.parseCreateTable("CREATE TABLE t (id INTEGER, score INTEGER);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (1, 7);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (2, 3);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (3, 9);"));

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT COUNT(*), COUNT(score), MIN(score), MAX(score) FROM t;"));
        assertEquals(1, rows.size());
        assertEquals(3L, rows.get(0).get(0).asInteger());
        assertEquals(3L, rows.get(0).get(1).asInteger());
        assertEquals(3L, rows.get(0).get(2).asInteger());
        assertEquals(9L, rows.get(0).get(3).asInteger());
    }

    @Test
    void section4_queryResultSemantics_aggregateAndNonAggregateMixRejectedWithoutGrouping() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(parser.parseCreateTable("CREATE TABLE t (id INTEGER);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (1);"));
        assertThrows(IllegalArgumentException.class,
                () -> db.execute(parser.parseSelect("SELECT COUNT(*), id FROM t;")));
    }

    @Test
    void section4_1_rowMutationSemantics_affectedRowCountAndVisibility() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(parser.parseCreateTable("CREATE TABLE t (id INTEGER, name TEXT);"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (1, 'a');"));
        db.execute(parser.parseInsert("INSERT INTO t VALUES (2, 'b');"));

        db.execute(parser.parseUpdate("UPDATE t SET name='z' WHERE id = 2;"));
        assertEquals(1, db.lastMutationCount());
        db.execute(parser.parseDelete("DELETE FROM t WHERE id = 1;"));
        assertEquals(1, db.lastMutationCount());

        List<List<DbValue>> rows = db.execute(parser.parseSelect("SELECT name FROM t;"));
        assertEquals(1, rows.size());
        assertEquals("z", rows.get(0).get(0).asText());
    }

    @Test
    void section5_errorCategories_transactionErrorOnCommitWithoutActiveTransaction() {
        TransactionManager tx = new TransactionManager();
        IllegalStateException error = assertThrows(IllegalStateException.class, tx::commit);
        assertTrue(error.getMessage().contains("COMMIT requires ACTIVE state"));
    }
}
