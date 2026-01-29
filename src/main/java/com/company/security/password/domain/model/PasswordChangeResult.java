package com.company.security.password.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object representing the result of a password change operation.
 * Immutable and with no external dependencies.
 */
public final class PasswordChangeResult {

    public enum ChangeType {
        RESET,      // Password reset using recovery token
        UPDATE      // Password update using current password
    }

    private final String userId;
    private final ChangeType changeType;
    private final Instant changedAt;
    private final boolean success;
    private final String message;

    private PasswordChangeResult(Builder builder) {
        this.userId = builder.userId;
        this.changeType = builder.changeType;
        this.changedAt = builder.changedAt;
        this.success = builder.success;
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PasswordChangeResult success(String userId, ChangeType changeType) {
        return builder()
                .userId(userId)
                .changeType(changeType)
                .changedAt(Instant.now())
                .success(true)
                .message("Password changed successfully")
                .build();
    }

    public static PasswordChangeResult failure(String userId, ChangeType changeType, String reason) {
        return builder()
                .userId(userId)
                .changeType(changeType)
                .changedAt(Instant.now())
                .success(false)
                .message(reason)
                .build();
    }

    public String userId() {
        return userId;
    }

    public ChangeType changeType() {
        return changeType;
    }

    public Instant changedAt() {
        return changedAt;
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordChangeResult that = (PasswordChangeResult) o;
        return success == that.success &&
               Objects.equals(userId, that.userId) &&
               changeType == that.changeType &&
               Objects.equals(changedAt, that.changedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, changeType, changedAt, success);
    }

    @Override
    public String toString() {
        return "PasswordChangeResult{" +
               "userId='" + userId + '\'' +
               ", changeType=" + changeType +
               ", changedAt=" + changedAt +
               ", success=" + success +
               '}';
    }

    public static final class Builder {
        private String userId;
        private ChangeType changeType;
        private Instant changedAt;
        private boolean success;
        private String message;

        private Builder() {}

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder changeType(ChangeType changeType) {
            this.changeType = changeType;
            return this;
        }

        public Builder changedAt(Instant changedAt) {
            this.changedAt = changedAt;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public PasswordChangeResult build() {
            Objects.requireNonNull(userId, "userId cannot be null");
            Objects.requireNonNull(changeType, "changeType cannot be null");
            if (changedAt == null) {
                changedAt = Instant.now();
            }
            return new PasswordChangeResult(this);
        }
    }
}
