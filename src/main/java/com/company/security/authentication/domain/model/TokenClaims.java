package com.company.security.authentication.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object representing the claims extracted from a JWT token.
 * Immutable and with no external dependencies.
 */
public final class TokenClaims {

    private final String jti;
    private final String subject;
    private final String userId;
    private final String username;
    private final String email;
    private final Set<String> roles;
    private final String deviceId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final String issuer;
    private final Map<String, Object> additionalClaims;

    private TokenClaims(Builder builder) {
        this.jti = builder.jti;
        this.subject = builder.subject;
        this.userId = builder.userId;
        this.username = builder.username;
        this.email = builder.email;
        this.roles = builder.roles != null
                ? Collections.unmodifiableSet(builder.roles)
                : Collections.emptySet();
        this.deviceId = builder.deviceId;
        this.issuedAt = builder.issuedAt;
        this.expiresAt = builder.expiresAt;
        this.issuer = builder.issuer;
        this.additionalClaims = builder.additionalClaims != null
                ? Collections.unmodifiableMap(builder.additionalClaims)
                : Collections.emptyMap();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String jti() {
        return jti;
    }

    public String subject() {
        return subject;
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

    public String deviceId() {
        return deviceId;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public String issuer() {
        return issuer;
    }

    public Map<String, Object> additionalClaims() {
        return additionalClaims;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public long remainingTimeInSeconds() {
        return Math.max(0, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenClaims that = (TokenClaims) o;
        return Objects.equals(jti, that.jti);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jti);
    }

    @Override
    public String toString() {
        return "TokenClaims{" +
               "jti='" + jti + '\'' +
               ", subject='" + subject + '\'' +
               ", userId='" + userId + '\'' +
               ", expiresAt=" + expiresAt +
               '}';
    }

    public static final class Builder {
        private String jti;
        private String subject;
        private String userId;
        private String username;
        private String email;
        private Set<String> roles;
        private String deviceId;
        private Instant issuedAt;
        private Instant expiresAt;
        private String issuer;
        private Map<String, Object> additionalClaims;

        private Builder() {}

        public Builder jti(String jti) {
            this.jti = jti;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
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

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Builder additionalClaims(Map<String, Object> additionalClaims) {
            this.additionalClaims = additionalClaims;
            return this;
        }

        public TokenClaims build() {
            Objects.requireNonNull(jti, "jti cannot be null");
            Objects.requireNonNull(subject, "subject cannot be null");
            Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
            return new TokenClaims(this);
        }
    }
}
