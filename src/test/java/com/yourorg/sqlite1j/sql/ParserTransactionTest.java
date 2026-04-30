package com.yourorg.sqlite1j.sql;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTransactionTest {
    @Test
    void parsesBeginCommitRollback() {
        Parser parser = new Parser();

        assertEquals(TransactionCommand.BEGIN, parser.parseTransactionControl("BEGIN;").command());
        assertEquals(TransactionCommand.COMMIT, parser.parseTransactionControl("COMMIT").command());
        assertEquals(TransactionCommand.ROLLBACK, parser.parseTransactionControl("ROLLBACK;").command());
    }

    @Test
    void failsOnUnsupportedCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> new Parser().parseTransactionControl("START TRANSACTION"));
    }
}
