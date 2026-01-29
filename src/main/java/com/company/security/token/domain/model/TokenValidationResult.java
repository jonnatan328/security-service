package com.company.security.token.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object representing the result of token validation.
 * Immutable and with no external dependencies.
 */
public final class TokenValidationResult {

    private final boolean valid;
    private final TokenStatus status;
    private final String userId;
    private final String username;
    private final String email;
    private final Set<String> roles;
    private final Instant expiresAt;
    private final String errorMessage;

    private TokenValidationResult(Builder builder) {
        this.valid = builder.valid;
        this.status = builder.status;
        this.userId = builder.userId;
        this.username = builder.username;
        this.email = builder.email;
        this.roles = builder.roles != null
                ? Collections.unmodifiableSet(builder.roles)
                : Collections.emptySet();
        this.expiresAt = builder.expiresAt;
        this.errorMessage = builder.errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static TokenValidationResult valid(Token token) {
        return builder()
                .valid(true)
                .status(TokenStatus.VALID)
                .userId(token.userId())
                .username(token.subject())
                .email(token.email())
                .roles(token.roles())
                .expiresAt(token.expiresAt())
                .build();
    }

    public static TokenValidationResult invalid(TokenStatus status, String errorMessage) {
        return builder()
                .valid(false)
                .status(status)
                .errorMessage(errorMessage)
                .build();
    }

    public static TokenValidationResult expired() {
        return invalid(TokenStatus.EXPIRED, "Token has expired");
    }

    public static TokenValidationResult revoked() {
        return invalid(TokenStatus.REVOKED, "Token has been revoked");
    }

    public static TokenValidationResult malformed() {
        return invalid(TokenStatus.MALFORMED, "Token is malformed");
    }

    public boolean valid() {
        return valid;
    }

    public TokenStatus status() {
        return status;
    }

    public String userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public Set<String> roles() {
        return roles;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public String errorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenValidationResult that = (TokenValidationResult) o;
        return valid == that.valid &&
               status == that.status &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, status, userId);
    }

    @Override
    public String toString() {
        return "TokenValidationResult{" +
               "valid=" + valid +
               ", status=" + status +
               ", userId='" + userId + '\'' +
               '}';
    }

    public static final class Builder {
        private boolean valid;
        private TokenStatus status;
        private String userId;
        private String username;
        private String email;
        private Set<String> roles;
        private Instant expiresAt;
        private String errorMessage;

        private Builder() {}

        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder status(TokenStatus status) {
            this.status = status;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public TokenValidationResult build() {
            Objects.requireNonNull(status, "status cannot be null");
            return new TokenValidationResult(this);
        }
    }
}
