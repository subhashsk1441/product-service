package com.example.product_service.exception;

/**
 * Exception thrown when input validation fails at the service or business layer.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
