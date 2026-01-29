package com.company.security.token.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenValidationResult Value Object")
class TokenValidationResultTest {

    @Test
    @DisplayName("Should create valid result from Token via valid() factory")
    void shouldCreateValidResult() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-1234567890abcdef")
                .subject("admin")
                .userId("user-1")
                .email("admin@example.com")
                .roles(Set.of("ROLE_ADMIN"))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        TokenValidationResult result = TokenValidationResult.valid(token);

        assertTrue(result.valid());
        assertEquals(TokenStatus.VALID, result.status());
        assertEquals("user-1", result.userId());
        assertEquals("admin", result.username());
        assertEquals("admin@example.com", result.email());
        assertTrue(result.roles().contains("ROLE_ADMIN"));
        assertNull(result.errorMessage());
    }

    @Test
    @DisplayName("Should create invalid result via invalid() factory")
    void shouldCreateInvalidResult() {
        TokenValidationResult result = TokenValidationResult.invalid(
                TokenStatus.INVALID, "Token signature mismatch");

        assertFalse(result.valid());
        assertEquals(TokenStatus.INVALID, result.status());
        assertEquals("Token signature mismatch", result.errorMessage());
        assertNull(result.userId());
    }

    @Test
    @DisplayName("Should create expired result via expired() factory")
    void shouldCreateExpiredResult() {
        TokenValidationResult result = TokenValidationResult.expired();

        assertFalse(result.valid());
        assertEquals(TokenStatus.EXPIRED, result.status());
        assertEquals("Token has expired", result.errorMessage());
    }

    @Test
    @DisplayName("Should create revoked result via revoked() factory")
    void shouldCreateRevokedResult() {
        TokenValidationResult result = TokenValidationResult.revoked();

        assertFalse(result.valid());
        assertEquals(TokenStatus.REVOKED, result.status());
        assertEquals("Token has been revoked", result.errorMessage());
    }

    @Test
    @DisplayName("Should have empty roles set when no roles provided")
    void shouldHaveEmptyRolesWhenNoneProvided() {
        TokenValidationResult result = TokenValidationResult.invalid(
                TokenStatus.EXPIRED, "Expired");

        assertNotNull(result.roles());
        assertTrue(result.roles().isEmpty());
    }
}
