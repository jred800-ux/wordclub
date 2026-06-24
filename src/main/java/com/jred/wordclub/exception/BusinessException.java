package com.jred.wordclub.exception;

/**
 * 业务逻辑异常 — 返回 400 Bad Request
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
