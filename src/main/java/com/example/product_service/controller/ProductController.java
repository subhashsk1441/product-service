package com.example.product_service.controller;

import com.example.product_service.dto.*;
import com.example.product_service.entity.ProductStatus;
import com.example.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Product Master Management.
 * <p>
 * Provides CRUD operations and search capabilities for products.
 * All endpoints require JWT authentication.
 * </p>
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product Master Management API")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a new product.
     *
     * @param request the product creation request
     * @return the newly created product with HTTP 201 Created
     */
    @Operation(summary = "Create a new product",
               description = "Creates a new product in the system. The product code must be unique.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Product with the given code already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a single product by ID.
     *
     * @param id the product ID
     * @return the product details with HTTP 200 OK
     */
    @Operation(summary = "Get product by ID",
               description = "Retrieves detailed information for a single product by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Retrieves all products with pagination and sorting support.
     *
     * @param page zero-based page index (default: 0)
     * @param size number of records per page (default: 20)
     * @param sort sort field name (default: name)
     * @return a page of products
     */
    @Operation(summary = "Get all products",
               description = "Retrieves a paginated list of all products. Supports page, size, and sort parameters.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    /**
     * Searches products using keyword, category, brand, and/or status filters.
     *
     * @param keyword  optional keyword to match against product name
     * @param category optional category filter
     * @param brand    optional brand filter
     * @param status   optional status filter
     * @param page     zero-based page index (default: 0)
     * @param size     number of records per page (default: 20)
     * @param sort     sort field name (default: name)
     * @return a page of matching products
     */
    @Operation(summary = "Search products",
               description = "Searches products by keyword (name), category, brand, or status. "
                       + "Keyword search takes precedence over other filters.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Keyword to search in product name") @RequestParam(required = false) String keyword,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Brand filter") @RequestParam(required = false) String brand,
            @Parameter(description = "Status filter") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sort) {

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .keyword(keyword)
                .category(category)
                .brand(brand)
                .status(status)
                .build();

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(productService.searchProducts(searchRequest, pageable));
    }

    /**
     * Updates an existing product.
     *
     * @param id      the ID of the product to update
     * @param request the product update request
     * @return the updated product with HTTP 200 OK
     */
    @Operation(summary = "Update an existing product",
               description = "Updates the details of an existing product identified by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
                     content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * Deletes a product by ID.
     *
     * @param id the ID of the product to delete
     * @return HTTP 204 No Content on success
     */
    @Operation(summary = "Delete a product",
               description = "Permanently deletes a product from the system by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT required")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
