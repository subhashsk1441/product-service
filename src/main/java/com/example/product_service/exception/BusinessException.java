package com.example.product_service.exception;

/**
 * Base exception for business rule violations.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
