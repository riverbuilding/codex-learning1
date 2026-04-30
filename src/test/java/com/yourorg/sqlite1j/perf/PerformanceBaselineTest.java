package com.yourorg.sqlite1j.perf;

import com.yourorg.sqlite1j.exec.InMemoryDatabase;
import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.sql.Tokenizer;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceBaselineTest {
    @Test
    void baselineTokenizerThroughput() {
        Tokenizer tokenizer = new Tokenizer();
        String sql = "SELECT id, name FROM users WHERE id = 100;";

        long start = System.nanoTime();
        int count = 20_000;
        for (int i = 0; i < count; i++) {
            tokenizer.tokenize(sql);
        }
        long elapsedNs = System.nanoTime() - start;
        double opsPerSec = count / (elapsedNs / 1_000_000_000.0);

        System.out.println("[baseline] tokenizer ops/sec=" + (long) opsPerSec);
        assertTrue(opsPerSec > 1_000, "Tokenizer baseline too low");
    }

    @Test
    void baselineInsertAndLookupFlow() {
        Parser parser = new Parser();
        InMemoryDatabase db = new InMemoryDatabase();
        db.execute(parser.parseCreateTable("CREATE TABLE t (name TEXT, age INTEGER);"));

        int rows = 2_000;
        long insertStart = System.nanoTime();
        for (int i = 0; i < rows; i++) {
            db.execute(parser.parseInsert("INSERT INTO t VALUES ('n" + i + "', " + i + ");"));
        }
        long insertElapsed = System.nanoTime() - insertStart;

        long queryStart = System.nanoTime();
        List<List<DbValue>> out = db.execute(parser.parseSelect("SELECT name FROM t WHERE age > 1990;"));
        long queryElapsed = System.nanoTime() - queryStart;

        double insertRowsPerSec = rows / (insertElapsed / 1_000_000_000.0);
        System.out.println("[baseline] insert rows/sec=" + (long) insertRowsPerSec);
        System.out.println("[baseline] query ms=" + (queryElapsed / 1_000_000.0));

        assertTrue(out.size() > 0);
        assertTrue(insertRowsPerSec > 100, "Insert baseline too low");
    }
}
