package com.yourorg.sqlite1j.exec;

import com.yourorg.sqlite1j.sql.Parser;
import com.yourorg.sqlite1j.types.DbValue;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrencyDurabilityEnvelopeTest {
    @Test
    void serializedStatementExecutionIsDeterministicUnderWriteStress() throws Exception {
        final Parser parser = new Parser();
        final InMemoryDatabase db = new InMemoryDatabase();
        db.execute(parser.parseCreateTable("CREATE TABLE t (id INT);"));

        int threads = 6;
        int insertsPerThread = 40;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        for (int i = 0; i < insertsPerThread; i++) {
                            db.executeStatementNormalized(parser.parseInsert("INSERT INTO t VALUES (" + (threadId * 1000 + i) + ");"));
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        done.countDown();
                    }
                }
            });
            workers.add(worker);
            worker.start();
        }

        start.countDown();
        done.await();
        for (Thread worker : workers) {
            worker.join();
        }

        List<List<DbValue>> countRows = db.execute(parser.parseSelect("SELECT COUNT(*) FROM t;"));
        assertEquals((long) threads * insertsPerThread, countRows.get(0).get(0).asInteger());
    }

    @Test
    void rollbackAndDurableRestartRemainConsistentAfterConcurrentMutations() throws Exception {
        final Parser parser = new Parser();
        Path file = Files.createTempFile("sqlite1j-envelope", ".db");
        final FileBackedDatabase db = new FileBackedDatabase(file);
        db.executeStatementNormalized(parser.parseCreateTable("CREATE TABLE t (id INT);"));

        db.executeStatementNormalized(parser.parseTransactionControl("BEGIN;"));
        int threads = 4;
        int insertsPerThread = 20;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        start.await();
                        for (int i = 0; i < insertsPerThread; i++) {
                            db.executeStatementNormalized(parser.parseInsert("INSERT INTO t VALUES (" + (threadId * 100 + i) + ");"));
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        done.countDown();
                    }
                }
            }).start();
        }
        start.countDown();
        done.await();
        db.executeStatementNormalized(parser.parseTransactionControl("ROLLBACK;"));

        FileBackedDatabase reopened = new FileBackedDatabase(file);
        List<List<DbValue>> countRows = reopened.executeStatementNormalized(parser.parseSelect("SELECT COUNT(*) FROM t;"));
        assertEquals(0L, countRows.get(0).get(0).asInteger());
    }
}
