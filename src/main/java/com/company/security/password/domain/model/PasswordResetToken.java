package com.company.security.password.domain.model;

import com.company.security.shared.domain.model.Email;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a password reset token.
 * Immutable and with no external dependencies.
 */
public final class PasswordResetToken {

    public enum Status {
        PENDING,
        USED,
        EXPIRED,
        CANCELLED
    }

    private final String id;
    private final String token;
    private final String userId;
    private final Email email;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final Instant usedAt;
    private final Status status;

    private PasswordResetToken(Builder builder) {
        this.id = builder.id;
        this.token = builder.token;
        this.userId = builder.userId;
        this.email = builder.email;
        this.createdAt = builder.createdAt;
        this.expiresAt = builder.expiresAt;
        this.usedAt = builder.usedAt;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PasswordResetToken create(String userId, Email email, long expirationMinutes) {
        Instant now = Instant.now();
        return builder()
                .id(UUID.randomUUID().toString())
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .email(email)
                .createdAt(now)
                .expiresAt(now.plusSeconds(expirationMinutes * 60))
                .status(Status.PENDING)
                .build();
    }

    public String id() {
        return id;
    }

    public String token() {
        return token;
    }

    public String userId() {
        return userId;
    }

    public Email email() {
        return email;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant usedAt() {
        return usedAt;
    }

    public Status status() {
        return status;
    }

    public boolean isExpired() {
        return status == Status.EXPIRED || Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return status == Status.USED;
    }

    public boolean isPending() {
        return status == Status.PENDING && !isExpired();
    }

    public boolean isValid() {
        return isPending();
    }

    public PasswordResetToken markAsUsed() {
        return builder()
                .id(this.id)
                .token(this.token)
                .userId(this.userId)
                .email(this.email)
                .createdAt(this.createdAt)
                .expiresAt(this.expiresAt)
                .usedAt(Instant.now())
                .status(Status.USED)
                .build();
    }

    public PasswordResetToken markAsCancelled() {
        return builder()
                .id(this.id)
                .token(this.token)
                .userId(this.userId)
                .email(this.email)
                .createdAt(this.createdAt)
                .expiresAt(this.expiresAt)
                .usedAt(this.usedAt)
                .status(Status.CANCELLED)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordResetToken that = (PasswordResetToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
               "id='" + id + '\'' +
               ", userId='" + userId + '\'' +
               ", email=" + email +
               ", status=" + status +
               ", expiresAt=" + expiresAt +
               '}';
    }

    public static final class Builder {
        private String id;
        private String token;
        private String userId;
        private Email email;
        private Instant createdAt;
        private Instant expiresAt;
        private Instant usedAt;
        private Status status = Status.PENDING;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(Email email) {
            this.email = email;
            return this;
        }

        public Builder email(String email) {
            this.email = Email.of(email);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder usedAt(Instant usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public PasswordResetToken build() {
            Objects.requireNonNull(token, "token cannot be null");
            Objects.requireNonNull(userId, "userId cannot be null");
            Objects.requireNonNull(email, "email cannot be null");
            Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
            return new PasswordResetToken(this);
        }
    }
}
