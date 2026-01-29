package com.company.security.authentication.infrastructure.adapter.input.rest.dto.response;

/**
 * Response DTO for token refresh operation.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
