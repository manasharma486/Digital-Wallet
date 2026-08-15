package com.wallet.exception;

public class UnauthorizedTransactionException extends RuntimeException {
    public UnauthorizedTransactionException(String message) {
        super(message);
    }
}
