package com.example.product_service.dto;

import com.example.product_service.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a product response returned from the API.
 * <p>
 * This class is used to transfer product data to clients without
 * exposing the internal entity structure.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String manufacturer;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
