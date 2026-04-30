package com.yourorg.sqlite1j.txn;

public final class TransactionManager {
    private final TransactionStateMachine machine = new TransactionStateMachine();

    public TransactionState state() {
        return machine.state();
    }

    public void begin() {
        machine.begin();
    }

    public void commit() {
        machine.beginCommit();
        machine.finishCommit();
    }

    public void rollback() {
        machine.beginRollback();
        machine.finishRollback();
    }
}
