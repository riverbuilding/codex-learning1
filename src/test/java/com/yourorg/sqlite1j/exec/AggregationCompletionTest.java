package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.errors.DbException;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AggregationCompletionTest {
    @Test
    void supportsGroupByWithCountAndHaving() {
        Parser p = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(p.parseCreateTable("CREATE TABLE t (grp TEXT, score INT);"));
        db.execute(p.parseInsert("INSERT INTO t VALUES ('a', 1);"));
        db.execute(p.parseInsert("INSERT INTO t VALUES ('a', 2);"));
        db.execute(p.parseInsert("INSERT INTO t VALUES ('b', 7);"));

        List<List<DbValue>> rows = db.execute(p.parseSelect(
                "SELECT grp, COUNT(*), MIN(score), MAX(score) FROM t GROUP BY grp HAVING COUNT(*) >= 2;"));
        assertEquals(1, rows.size());
        assertEquals("a", rows.get(0).get(0).asText());
        assertEquals(2L, rows.get(0).get(1).asInteger());
        assertEquals(1L, rows.get(0).get(2).asInteger());
        assertEquals(2L, rows.get(0).get(3).asInteger());
    }

    @Test
    void mixedProjectionWithoutGroupByFailsAsSchemaError() {
        Parser p = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(p.parseCreateTable("CREATE TABLE t (grp TEXT, score INT);"));
        db.execute(p.parseInsert("INSERT INTO t VALUES ('a', 1);"));

        DbException ex = assertThrows(DbException.class,
                () -> db.executeStatementNormalized(p.parseSelect("SELECT grp, COUNT(*) FROM t;")));
        assertEquals("SCHEMA_ERROR", ex.error().code());
    }
}
