package com.company.security.authentication.domain.model;

import java.util.Objects;

/**
 * Value Object representing the result of a successful authentication.
 * Contains both the authenticated user and the generated token pair.
 * Immutable and with no external dependencies.
 */
public final class AuthenticationResult {

    private final AuthenticatedUser user;
    private final TokenPair tokenPair;

    private AuthenticationResult(AuthenticatedUser user, TokenPair tokenPair) {
        this.user = user;
        this.tokenPair = tokenPair;
    }

    public static AuthenticationResult of(AuthenticatedUser user, TokenPair tokenPair) {
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(tokenPair, "tokenPair cannot be null");
        return new AuthenticationResult(user, tokenPair);
    }

    public AuthenticatedUser user() {
        return user;
    }

    public TokenPair tokenPair() {
        return tokenPair;
    }

    public String accessToken() {
        return tokenPair.accessToken();
    }

    public String refreshToken() {
        return tokenPair.refreshToken();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationResult that = (AuthenticationResult) o;
        return Objects.equals(user, that.user) &&
               Objects.equals(tokenPair, that.tokenPair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, tokenPair);
    }

    @Override
    public String toString() {
        return "AuthenticationResult{" +
               "user=" + user +
               ", tokenPair=" + tokenPair +
               '}';
    }
}
