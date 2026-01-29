package com.company.security.authentication.infrastructure.adapter.input.rest.dto.request;

/**
 * Request DTO for sign-out operation.
 */
public record SignOutRequest(
        String refreshToken
) {
}
