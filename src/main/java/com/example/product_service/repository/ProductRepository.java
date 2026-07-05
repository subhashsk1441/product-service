package com.example.product_service.repository;

import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Product entity providing CRUD and query operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Checks if a product with the given code already exists.
     *
     * @param code the product code to check
     * @return true if the code is already in use, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Finds a product by its unique code.
     *
     * @param code the product code
     * @return an Optional containing the product if found
     */
    Optional<Product> findByCode(String code);

    /**
     * Finds products whose name contains the given keyword (case-insensitive).
     *
     * @param name     the keyword to search in product names
     * @param pageable pagination and sorting information
     * @return a page of matching products
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Finds all products belonging to a specific category.
     *
     * @param category the category to filter by
     * @param pageable pagination and sorting information
     * @return a page of products in the given category
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Finds all products from a specific brand.
     *
     * @param brand    the brand to filter by
     * @param pageable pagination and sorting information
     * @return a page of products from the given brand
     */
    Page<Product> findByBrand(String brand, Pageable pageable);

    /**
     * Finds all products with a specific status.
     *
     * @param status   the status to filter by
     * @param pageable pagination and sorting information
     * @return a page of products with the given status
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}
