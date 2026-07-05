package com.example.product_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Product Service application.
 * <p>
 * This microservice is responsible for Product Master Management only.
 * It does NOT manage inventory, stock, orders, purchases, billing, pricing, or warehouses.
 * </p>
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
