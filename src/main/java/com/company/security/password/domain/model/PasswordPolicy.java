package com.company.security.password.domain.model;

/**
 * Value Object representing password policy configuration.
 * Immutable and with no external dependencies.
 */
public final class PasswordPolicy {

    private final int minLength;
    private final int maxLength;
    private final boolean requireUppercase;
    private final boolean requireLowercase;
    private final boolean requireDigit;
    private final boolean requireSpecialChar;
    private final int maxHistoryCount;

    private PasswordPolicy(Builder builder) {
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.requireUppercase = builder.requireUppercase;
        this.requireLowercase = builder.requireLowercase;
        this.requireDigit = builder.requireDigit;
        this.requireSpecialChar = builder.requireSpecialChar;
        this.maxHistoryCount = builder.maxHistoryCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PasswordPolicy defaultPolicy() {
        return builder()
                .minLength(8)
                .maxLength(128)
                .requireUppercase(true)
                .requireLowercase(true)
                .requireDigit(true)
                .requireSpecialChar(true)
                .maxHistoryCount(5)
                .build();
    }

    public int minLength() {
        return minLength;
    }

    public int maxLength() {
        return maxLength;
    }

    public boolean requireUppercase() {
        return requireUppercase;
    }

    public boolean requireLowercase() {
        return requireLowercase;
    }

    public boolean requireDigit() {
        return requireDigit;
    }

    public boolean requireSpecialChar() {
        return requireSpecialChar;
    }

    public int maxHistoryCount() {
        return maxHistoryCount;
    }

    @Override
    public String toString() {
        return "PasswordPolicy{" +
               "minLength=" + minLength +
               ", maxLength=" + maxLength +
               ", requireUppercase=" + requireUppercase +
               ", requireLowercase=" + requireLowercase +
               ", requireDigit=" + requireDigit +
               ", requireSpecialChar=" + requireSpecialChar +
               ", maxHistoryCount=" + maxHistoryCount +
               '}';
    }

    public static final class Builder {
        private int minLength = 8;
        private int maxLength = 128;
        private boolean requireUppercase = true;
        private boolean requireLowercase = true;
        private boolean requireDigit = true;
        private boolean requireSpecialChar = true;
        private int maxHistoryCount = 5;

        private Builder() {}

        public Builder minLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder requireUppercase(boolean requireUppercase) {
            this.requireUppercase = requireUppercase;
            return this;
        }

        public Builder requireLowercase(boolean requireLowercase) {
            this.requireLowercase = requireLowercase;
            return this;
        }

        public Builder requireDigit(boolean requireDigit) {
            this.requireDigit = requireDigit;
            return this;
        }

        public Builder requireSpecialChar(boolean requireSpecialChar) {
            this.requireSpecialChar = requireSpecialChar;
            return this;
        }

        public Builder maxHistoryCount(int maxHistoryCount) {
            this.maxHistoryCount = maxHistoryCount;
            return this;
        }

        public PasswordPolicy build() {
            if (minLength < 1) {
                throw new IllegalArgumentException("minLength must be at least 1");
            }
            if (maxLength < minLength) {
                throw new IllegalArgumentException("maxLength must be >= minLength");
            }
            return new PasswordPolicy(this);
        }
    }
}
