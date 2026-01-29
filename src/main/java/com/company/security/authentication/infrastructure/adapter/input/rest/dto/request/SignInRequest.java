package com.company.security.authentication.infrastructure.adapter.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sign-in operation.
 */
public record SignInRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 1, max = 100, message = "Username must be between 1 and 100 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 1, max = 256, message = "Password must be between 1 and 256 characters")
        String password,

        @Size(max = 100, message = "Device ID must not exceed 100 characters")
        String deviceId
) {
}
