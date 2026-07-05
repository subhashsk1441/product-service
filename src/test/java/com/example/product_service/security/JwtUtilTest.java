package com.example.product_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtUtil}.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET =
            "test-secret-key-must-be-at-least-256-bits-for-hs256-algorithm";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    @DisplayName("validateToken - returns false for invalid token")
    void validateToken_invalidToken_returnsFalse() {
        boolean valid = jwtUtil.validateToken("not.a.valid.token");
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validateToken - returns false for empty token")
    void validateToken_emptyToken_returnsFalse() {
        boolean valid = jwtUtil.validateToken("");
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validateToken - returns false for null token")
    void validateToken_nullToken_returnsFalse() {
        boolean valid = jwtUtil.validateToken(null);
        assertThat(valid).isFalse();
    }
}
