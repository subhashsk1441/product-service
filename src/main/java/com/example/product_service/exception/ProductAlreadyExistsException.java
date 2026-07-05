package com.example.product_service.exception;

/**
 * Exception thrown when attempting to create a product with a code that already exists.
 */
public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    public ProductAlreadyExistsException(String code, String field) {
        super("Product already exists with " + field + ": " + code);
    }
}
