package com.company.security.authentication.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object representing a pair of access and refresh tokens.
 * Immutable and with no external dependencies.
 */
public final class TokenPair {

    private final String accessToken;
    private final String refreshToken;
    private final Instant accessTokenExpiresAt;
    private final Instant refreshTokenExpiresAt;
    private final String tokenType;

    private TokenPair(Builder builder) {
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.accessTokenExpiresAt = builder.accessTokenExpiresAt;
        this.refreshTokenExpiresAt = builder.refreshTokenExpiresAt;
        this.tokenType = builder.tokenType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public Instant accessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public Instant refreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public String tokenType() {
        return tokenType;
    }

    public long accessTokenExpiresInSeconds() {
        return Math.max(0, accessTokenExpiresAt.getEpochSecond() - Instant.now().getEpochSecond());
    }

    public boolean isAccessTokenExpired() {
        return Instant.now().isAfter(accessTokenExpiresAt);
    }

    public boolean isRefreshTokenExpired() {
        return Instant.now().isAfter(refreshTokenExpiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenPair tokenPair = (TokenPair) o;
        return Objects.equals(accessToken, tokenPair.accessToken) &&
               Objects.equals(refreshToken, tokenPair.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken);
    }

    @Override
    public String toString() {
        return "TokenPair{" +
               "tokenType='" + tokenType + '\'' +
               ", accessTokenExpiresAt=" + accessTokenExpiresAt +
               ", refreshTokenExpiresAt=" + refreshTokenExpiresAt +
               '}';
    }

    public static final class Builder {
        private String accessToken;
        private String refreshToken;
        private Instant accessTokenExpiresAt;
        private Instant refreshTokenExpiresAt;
        private String tokenType = "Bearer";

        private Builder() {}

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder accessTokenExpiresAt(Instant accessTokenExpiresAt) {
            this.accessTokenExpiresAt = accessTokenExpiresAt;
            return this;
        }

        public Builder refreshTokenExpiresAt(Instant refreshTokenExpiresAt) {
            this.refreshTokenExpiresAt = refreshTokenExpiresAt;
            return this;
        }

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TokenPair build() {
            Objects.requireNonNull(accessToken, "accessToken cannot be null");
            Objects.requireNonNull(refreshToken, "refreshToken cannot be null");
            Objects.requireNonNull(accessTokenExpiresAt, "accessTokenExpiresAt cannot be null");
            Objects.requireNonNull(refreshTokenExpiresAt, "refreshTokenExpiresAt cannot be null");
            return new TokenPair(this);
        }
    }
}
