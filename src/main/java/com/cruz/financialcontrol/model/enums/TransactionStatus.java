package com.cruz.financialcontrol.model.enums;

public enum TransactionStatus {
    PENDING,
    CONFIRMED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
}
