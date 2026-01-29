package com.company.security.authentication.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for token refresh operation.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
