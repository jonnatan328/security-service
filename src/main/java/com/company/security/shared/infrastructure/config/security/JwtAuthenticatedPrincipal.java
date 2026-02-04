package com.company.security.shared.infrastructure.config.security;

import com.company.security.authentication.domain.model.TokenClaims;

import java.security.Principal;
import java.util.Set;

/**
 * Principal implementation that wraps full TokenClaims from the JWT.
 * getName() returns the userId (not username) to align with domain expectations.
 */
public record JwtAuthenticatedPrincipal(TokenClaims tokenClaims) implements Principal {

    @Override
    public String getName() {
        return tokenClaims.userId();
    }

    public String getUserId() {
        return tokenClaims.userId();
    }

    public String getUsername() {
        return tokenClaims.username();
    }

    public String getEmail() {
        return tokenClaims.email();
    }

    public String getDeviceId() {
        return tokenClaims.deviceId();
    }

    public Set<String> getRoles() {
        return tokenClaims.roles();
    }
}
