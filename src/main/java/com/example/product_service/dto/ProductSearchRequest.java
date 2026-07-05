package com.example.product_service.dto;

import com.example.product_service.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO encapsulating search criteria for product queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    private String keyword;
    private String category;
    private String brand;
    private ProductStatus status;
}
