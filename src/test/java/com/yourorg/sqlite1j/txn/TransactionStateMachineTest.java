package com.yourorg.sqlite1j.txn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionStateMachineTest {
    @Test
    void validCommitFlow() {
        TransactionStateMachine m = new TransactionStateMachine();
        assertEquals(TransactionState.IDLE, m.state());

        m.begin();
        assertEquals(TransactionState.ACTIVE, m.state());

        m.beginCommit();
        assertEquals(TransactionState.COMMITTING, m.state());

        m.finishCommit();
        assertEquals(TransactionState.IDLE, m.state());
    }

    @Test
    void validRollbackFlow() {
        TransactionStateMachine m = new TransactionStateMachine();
        m.begin();
        m.beginRollback();
        assertEquals(TransactionState.ROLLING_BACK, m.state());
        m.finishRollback();
        assertEquals(TransactionState.IDLE, m.state());
    }

    @Test
    void invalidTransitionsFail() {
        TransactionStateMachine m = new TransactionStateMachine();
        assertThrows(IllegalStateException.class, m::beginCommit);
        assertThrows(IllegalStateException.class, m::finishCommit);
        assertThrows(IllegalStateException.class, m::beginRollback);
    }

    @Test
    void transactionManagerWrapsStateMachine() {
        TransactionManager tx = new TransactionManager();
        tx.begin();
        assertEquals(TransactionState.ACTIVE, tx.state());
        tx.commit();
        assertEquals(TransactionState.IDLE, tx.state());
    }
}
