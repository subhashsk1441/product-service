package com.example.product_service.service;

import com.example.product_service.dto.*;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductStatus;
import com.example.product_service.exception.ProductAlreadyExistsException;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service responsible for all business logic related to Product Master Management.
 * <p>
 * This service handles creating, updating, deleting, and searching products.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Creates a new product.
     *
     * @param request the product creation request containing product details
     * @return the created product as a {@link ProductResponse}
     * @throws ProductAlreadyExistsException if the product code is already in use
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByCode(request.getCode())) {
            throw new ProductAlreadyExistsException(request.getCode(), "code");
        }

        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .brand(request.getBrand())
                .manufacturer(request.getManufacturer())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: id={}, code={}", saved.getId(), saved.getCode());
        return mapToResponse(saved);
    }

    /**
     * Updates an existing product.
     *
     * @param id      the ID of the product to update
     * @param request the update request containing new product details
     * @return the updated product as a {@link ProductResponse}
     * @throws ProductNotFoundException if no product exists with the given ID
     */
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findProductById(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setManufacturer(request.getManufacturer());

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product updated = productRepository.save(product);
        log.info("Product updated: id={}, code={}", updated.getId(), updated.getCode());
        return mapToResponse(updated);
    }

    /**
     * Deletes a product by ID.
     *
     * @param id the ID of the product to delete
     * @throws ProductNotFoundException if no product exists with the given ID
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductById(id);
        productRepository.delete(product);
        log.info("Product deleted: id={}, code={}", product.getId(), product.getCode());
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product as a {@link ProductResponse}
     * @throws ProductNotFoundException if no product exists with the given ID
     */
    public ProductResponse getProductById(Long id) {
        return mapToResponse(findProductById(id));
    }

    /**
     * Retrieves all products with pagination support.
     *
     * @param pageable pagination and sorting configuration
     * @return a page of {@link ProductResponse} objects
     */
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    /**
     * Searches products based on the provided search criteria.
     * <p>
     * Keyword search takes precedence over other filters. If a keyword is provided,
     * the search is performed by name. Otherwise, category, brand, or status filters are applied.
     * If no filter is provided, all products are returned.
     * </p>
     *
     * @param searchRequest the search criteria
     * @param pageable      pagination and sorting configuration
     * @return a page of matching {@link ProductResponse} objects
     */
    public Page<ProductResponse> searchProducts(ProductSearchRequest searchRequest, Pageable pageable) {
        if (StringUtils.hasText(searchRequest.getKeyword())) {
            return productRepository
                    .findByNameContainingIgnoreCase(searchRequest.getKeyword(), pageable)
                    .map(this::mapToResponse);
        }

        if (StringUtils.hasText(searchRequest.getCategory())) {
            return productRepository
                    .findByCategory(searchRequest.getCategory(), pageable)
                    .map(this::mapToResponse);
        }

        if (StringUtils.hasText(searchRequest.getBrand())) {
            return productRepository
                    .findByBrand(searchRequest.getBrand(), pageable)
                    .map(this::mapToResponse);
        }

        if (searchRequest.getStatus() != null) {
            return productRepository
                    .findByStatus(searchRequest.getStatus(), pageable)
                    .map(this::mapToResponse);
        }

        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .manufacturer(product.getManufacturer())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
