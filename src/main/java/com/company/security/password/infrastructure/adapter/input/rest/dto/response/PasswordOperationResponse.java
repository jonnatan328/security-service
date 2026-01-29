package com.company.security.password.infrastructure.adapter.input.rest.dto.response;

import java.time.Instant;

public record PasswordOperationResponse(
        boolean success,
        String message,
        Instant timestamp
) {
    public static PasswordOperationResponse success(String message) {
        return new PasswordOperationResponse(true, message, Instant.now());
    }

    public static PasswordOperationResponse failure(String message) {
        return new PasswordOperationResponse(false, message, Instant.now());
    }
}
