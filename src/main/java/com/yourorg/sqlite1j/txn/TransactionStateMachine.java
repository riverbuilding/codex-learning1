package com.yourorg.sqlite1j.txn;

public final class TransactionStateMachine {
    private TransactionState state = TransactionState.IDLE;

    public TransactionState state() {
        return state;
    }

    public void begin() {
        if (state == TransactionState.ACTIVE) {
            throw new IllegalStateException("Nested transactions are not supported; use COMMIT or ROLLBACK before BEGIN");
        }
        ensureState(TransactionState.IDLE, "BEGIN requires IDLE state");
        state = TransactionState.ACTIVE;
    }

    public void beginCommit() {
        ensureState(TransactionState.ACTIVE, "COMMIT requires ACTIVE state");
        state = TransactionState.COMMITTING;
    }

    public void finishCommit() {
        ensureState(TransactionState.COMMITTING, "finishCommit requires COMMITTING state");
        state = TransactionState.IDLE;
    }

    public void beginRollback() {
        ensureState(TransactionState.ACTIVE, "ROLLBACK requires ACTIVE state");
        state = TransactionState.ROLLING_BACK;
    }

    public void finishRollback() {
        ensureState(TransactionState.ROLLING_BACK, "finishRollback requires ROLLING_BACK state");
        state = TransactionState.IDLE;
    }

    private void ensureState(TransactionState expected, String message) {
        if (state != expected) {
            throw new IllegalStateException(message + "; current=" + state);
        }
    }
}
