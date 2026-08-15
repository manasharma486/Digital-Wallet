package com.wallet.exception;

public class WalletFrozenException extends RuntimeException {
    public WalletFrozenException(String message) {
        super(message);
    }
}
