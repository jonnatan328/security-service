package com.company.security.token.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}
