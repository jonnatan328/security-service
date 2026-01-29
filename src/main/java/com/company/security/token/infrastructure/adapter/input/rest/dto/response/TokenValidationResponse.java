package com.company.security.token.infrastructure.adapter.input.rest.dto.response;

import java.time.Instant;
import java.util.Set;

public record TokenValidationResponse(
        boolean valid,
        String status,
        String userId,
        String username,
        String email,
        Set<String> roles,
        Instant expiresAt,
        String errorMessage
) {
}
