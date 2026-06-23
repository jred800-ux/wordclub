package com.jred.wordclub.exception;

/**
 * IP 频率限制异常
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
