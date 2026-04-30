package com.yourorg.sqlite1j.txn;

public enum TransactionState {
    IDLE,
    ACTIVE,
    COMMITTING,
    ROLLING_BACK
}
