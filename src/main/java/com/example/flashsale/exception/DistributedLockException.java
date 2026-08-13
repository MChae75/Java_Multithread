package com.example.flashsale.exception;

public class DistributedLockException extends RuntimeException {
    public DistributedLockException(String message) {
        super(message);
    }

    public DistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
