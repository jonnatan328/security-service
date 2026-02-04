package com.company.security.authentication.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TokenPair Value Object")
class TokenPairTest {

    @Test
    @DisplayName("Should build a valid TokenPair with all fields")
    void shouldBuildValidTokenPair() {
        Instant accessExpiry = Instant.now().plusSeconds(3600);
        Instant refreshExpiry = Instant.now().plusSeconds(86400);

        TokenPair tokenPair = TokenPair.builder()
                .accessToken("access-token-value")
                .refreshToken("refresh-token-value")
                .accessTokenExpiresAt(accessExpiry)
                .refreshTokenExpiresAt(refreshExpiry)
                .build();

        assertEquals("access-token-value", tokenPair.accessToken());
        assertEquals("refresh-token-value", tokenPair.refreshToken());
        assertEquals(accessExpiry, tokenPair.accessTokenExpiresAt());
        assertEquals(refreshExpiry, tokenPair.refreshTokenExpiresAt());
        assertEquals("Bearer", tokenPair.tokenType());
    }

    @Test
    @DisplayName("Should return positive accessTokenExpiresInSeconds for future token")
    void shouldReturnPositiveExpiresInSeconds() {
        TokenPair tokenPair = TokenPair.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .accessTokenExpiresAt(Instant.now().plusSeconds(3600))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .build();

        assertTrue(tokenPair.accessTokenExpiresInSeconds() > 0);
    }

    @Test
    @DisplayName("Should return zero accessTokenExpiresInSeconds for expired token")
    void shouldReturnZeroExpiresInSecondsForExpired() {
        TokenPair tokenPair = TokenPair.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .accessTokenExpiresAt(Instant.now().minusSeconds(100))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .build();

        assertEquals(0, tokenPair.accessTokenExpiresInSeconds());
    }

    @Test
    @DisplayName("Should detect access token as not expired for future expiry")
    void shouldDetectNotExpired() {
        TokenPair tokenPair = TokenPair.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .accessTokenExpiresAt(Instant.now().plusSeconds(3600))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .build();

        assertFalse(tokenPair.isAccessTokenExpired());
    }

    @Test
    @DisplayName("Should detect access token as expired for past expiry")
    void shouldDetectExpired() {
        TokenPair tokenPair = TokenPair.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .accessTokenExpiresAt(Instant.now().minusSeconds(100))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .build();

        assertTrue(tokenPair.isAccessTokenExpired());
    }

    @Test
    @DisplayName("Should throw NullPointerException when accessToken is null")
    void shouldThrowWhenAccessTokenNull() {
        assertThrows(NullPointerException.class, () ->
                TokenPair.builder()
                        .refreshToken("refresh")
                        .accessTokenExpiresAt(Instant.now().plusSeconds(3600))
                        .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                        .build());
    }

    @Test
    @DisplayName("Should throw NullPointerException when refreshToken is null")
    void shouldThrowWhenRefreshTokenNull() {
        assertThrows(NullPointerException.class, () ->
                TokenPair.builder()
                        .accessToken("access")
                        .accessTokenExpiresAt(Instant.now().plusSeconds(3600))
                        .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                        .build());
    }

    @Test
    @DisplayName("Should throw NullPointerException when accessTokenExpiresAt is null")
    void shouldThrowWhenAccessExpiresAtNull() {
        assertThrows(NullPointerException.class, () ->
                TokenPair.builder()
                        .accessToken("access")
                        .refreshToken("refresh")
                        .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                        .build());
    }

    @Test
    @DisplayName("Should throw NullPointerException when refreshTokenExpiresAt is null")
    void shouldThrowWhenRefreshExpiresAtNull() {
        assertThrows(NullPointerException.class, () ->
                TokenPair.builder()
                        .accessToken("access")
                        .refreshToken("refresh")
                        .accessTokenExpiresAt(Instant.now().plusSeconds(3600))
                        .build());
    }
}
