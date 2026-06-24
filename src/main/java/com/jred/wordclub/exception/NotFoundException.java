package com.jred.wordclub.exception;

/**
 * 资源不存在异常 — 返回 404 Not Found
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
