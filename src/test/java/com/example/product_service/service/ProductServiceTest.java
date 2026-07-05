package com.example.product_service.service;

import com.example.product_service.dto.*;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.ProductStatus;
import com.example.product_service.exception.ProductAlreadyExistsException;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductService}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .code("PROD-001")
                .name("Test Product")
                .description("A test product")
                .category("Electronics")
                .brand("TestBrand")
                .manufacturer("TestCo")
                .status(ProductStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // -------------------------------------------------------------------------
    // createProduct
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createProduct - success")
    void createProduct_success() {
        CreateProductRequest request = CreateProductRequest.builder()
                .code("PROD-001")
                .name("Test Product")
                .category("Electronics")
                .brand("TestBrand")
                .build();

        when(productRepository.existsByCode("PROD-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("PROD-001");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);

        verify(productRepository).existsByCode("PROD-001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct - throws ProductAlreadyExistsException when code is duplicate")
    void createProduct_duplicateCode_throwsException() {
        CreateProductRequest request = CreateProductRequest.builder()
                .code("PROD-001")
                .name("Another Product")
                .build();

        when(productRepository.existsByCode("PROD-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ProductAlreadyExistsException.class);

        verify(productRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateProduct
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateProduct - success")
    void updateProduct_success() {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Product")
                .category("Updated Category")
                .brand("Updated Brand")
                .status(ProductStatus.INACTIVE)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct - throws ProductNotFoundException when product not found")
    void updateProduct_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Any Name")
                .build();

        assertThatThrownBy(() -> productService.updateProduct(99L, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // deleteProduct
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteProduct - success")
    void deleteProduct_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThatCode(() -> productService.deleteProduct(1L)).doesNotThrowAnyException();

        verify(productRepository).delete(sampleProduct);
    }

    @Test
    @DisplayName("deleteProduct - throws ProductNotFoundException when product not found")
    void deleteProduct_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // getProductById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getProductById - success")
    void getProductById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCode()).isEqualTo("PROD-001");
    }

    @Test
    @DisplayName("getProductById - throws ProductNotFoundException when product not found")
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // getAllProducts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllProducts - returns paged results")
    void getAllProducts_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductResponse> result = productService.getAllProducts(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getCode()).isEqualTo("PROD-001");
    }

    // -------------------------------------------------------------------------
    // searchProducts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("searchProducts - keyword search returns matching products")
    void searchProducts_byKeyword() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findByNameContainingIgnoreCase("Test", pageable)).thenReturn(page);

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .keyword("Test")
                .build();

        Page<ProductResponse> result = productService.searchProducts(searchRequest, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchProducts - category search returns matching products")
    void searchProducts_byCategory() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findByCategory("Electronics", pageable)).thenReturn(page);

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .category("Electronics")
                .build();

        Page<ProductResponse> result = productService.searchProducts(searchRequest, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchProducts - brand search returns matching products")
    void searchProducts_byBrand() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findByBrand("TestBrand", pageable)).thenReturn(page);

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .brand("TestBrand")
                .build();

        Page<ProductResponse> result = productService.searchProducts(searchRequest, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchProducts - status search returns matching products")
    void searchProducts_byStatus() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findByStatus(ProductStatus.ACTIVE, pageable)).thenReturn(page);

        ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                .status(ProductStatus.ACTIVE)
                .build();

        Page<ProductResponse> result = productService.searchProducts(searchRequest, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchProducts - no filter returns all products")
    void searchProducts_noFilter_returnsAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct));

        when(productRepository.findAll(pageable)).thenReturn(page);

        ProductSearchRequest searchRequest = new ProductSearchRequest();

        Page<ProductResponse> result = productService.searchProducts(searchRequest, pageable);

        assertThat(result).hasSize(1);
    }
}
