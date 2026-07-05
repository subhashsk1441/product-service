package com.example.product_service.controller;

import com.example.product_service.dto.*;
import com.example.product_service.entity.ProductStatus;
import com.example.product_service.exception.GlobalExceptionHandler;
import com.example.product_service.exception.ProductAlreadyExistsException;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.security.JwtAuthenticationFilter;
import com.example.product_service.security.JwtUtil;
import com.example.product_service.security.SecurityConfig;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ProductController}.
 * <p>
 * Uses {@link WebMvcTest} to test only the web layer with mocked security
 * and a mocked {@link ProductService}.
 * </p>
 */
@WebMvcTest(controllers = ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    // Required so that JwtAuthenticationFilter (a @Component) can be loaded
    // in the @WebMvcTest slice without needing the real jwt.secret property.
    @MockBean
    private JwtUtil jwtUtil;

    private ProductResponse sampleResponse() {
        return ProductResponse.builder()
                .id(1L)
                .code("PROD-001")
                .name("Test Product")
                .description("A test product")
                .category("Electronics")
                .brand("TestBrand")
                .manufacturer("TestCo")
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /api/products
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/products - creates product successfully")
    @WithMockUser
    void createProduct_success() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .code("PROD-001")
                .name("Test Product")
                .category("Electronics")
                .brand("TestBrand")
                .build();

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("PROD-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("POST /api/products - returns 400 when name is blank")
    @WithMockUser
    void createProduct_blankName_returns400() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .code("PROD-001")
                .name("")
                .build();

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/products - returns 409 when product code already exists")
    @WithMockUser
    void createProduct_duplicateCode_returns409() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .code("PROD-001")
                .name("Test Product")
                .build();

        when(productService.createProduct(any())).thenThrow(new ProductAlreadyExistsException("PROD-001", "code"));

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/products - returns 401 when unauthenticated")
    void createProduct_unauthenticated_returns401() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .code("PROD-001")
                .name("Test Product")
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // GET /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/products/{id} - returns product by ID")
    @WithMockUser
    void getProductById_success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("PROD-001"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - returns 404 when product not found")
    @WithMockUser
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/products
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/products - returns paged list")
    @WithMockUser
    void getAllProducts_success() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("PROD-001"));
    }

    // -------------------------------------------------------------------------
    // GET /api/products/search
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/products/search - searches by keyword")
    @WithMockUser
    void searchProducts_byKeyword_success() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(productService.searchProducts(any(ProductSearchRequest.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("PROD-001"));
    }

    // -------------------------------------------------------------------------
    // PUT /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/products/{id} - updates product successfully")
    @WithMockUser
    void updateProduct_success() throws Exception {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Product")
                .category("Updated Category")
                .build();

        when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - returns 404 when product not found")
    @WithMockUser
    void updateProduct_notFound_returns404() throws Exception {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Product")
                .build();

        when(productService.updateProduct(eq(99L), any())).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(put("/api/products/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/products/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/products/{id} - deletes product successfully")
    @WithMockUser
    void deleteProduct_success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - returns 404 when product not found")
    @WithMockUser
    void deleteProduct_notFound_returns404() throws Exception {
        doThrow(new ProductNotFoundException(99L)).when(productService).deleteProduct(99L);

        mockMvc.perform(delete("/api/products/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
