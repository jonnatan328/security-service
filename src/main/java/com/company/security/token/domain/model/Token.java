package com.company.security.token.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object representing a JWT token for validation purposes.
 * Immutable and with no external dependencies.
 */
public final class Token {

    private final String rawToken;
    private final String jti;
    private final String subject;
    private final String userId;
    private final String email;
    private final Set<String> roles;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final String issuer;

    private Token(Builder builder) {
        this.rawToken = builder.rawToken;
        this.jti = builder.jti;
        this.subject = builder.subject;
        this.userId = builder.userId;
        this.email = builder.email;
        this.roles = builder.roles != null
                ? Collections.unmodifiableSet(builder.roles)
                : Collections.emptySet();
        this.issuedAt = builder.issuedAt;
        this.expiresAt = builder.expiresAt;
        this.issuer = builder.issuer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String rawToken() {
        return rawToken;
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

    public String email() {
        return email;
    }

    public Set<String> roles() {
        return roles;
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

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public long remainingTimeInSeconds() {
        if (expiresAt == null) {
            return 0;
        }
        return Math.max(0, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(jti, token.jti);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jti);
    }

    @Override
    public String toString() {
        return "Token{" +
               "jti='" + jti + '\'' +
               ", subject='" + subject + '\'' +
               ", userId='" + userId + '\'' +
               ", expiresAt=" + expiresAt +
               '}';
    }

    public static final class Builder {
        private String rawToken;
        private String jti;
        private String subject;
        private String userId;
        private String email;
        private Set<String> roles;
        private Instant issuedAt;
        private Instant expiresAt;
        private String issuer;

        private Builder() {}

        public Builder rawToken(String rawToken) {
            this.rawToken = rawToken;
            return this;
        }

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

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
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

        public Token build() {
            Objects.requireNonNull(rawToken, "rawToken cannot be null");
            Objects.requireNonNull(jti, "jti cannot be null");
            Objects.requireNonNull(subject, "subject cannot be null");
            return new Token(this);
        }
    }
}
