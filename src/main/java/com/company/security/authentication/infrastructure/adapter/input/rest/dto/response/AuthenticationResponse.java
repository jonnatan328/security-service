package com.company.security.authentication.infrastructure.adapter.input.rest.dto.response;

import java.util.Set;

/**
 * Response DTO for successful authentication.
 */
public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    public record UserInfo(
            String userId,
            String username,
            String email,
            String firstName,
            String lastName,
            Set<String> roles
    ) {
    }
}
