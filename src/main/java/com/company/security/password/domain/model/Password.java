package com.company.security.password.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a password with validation rules.
 * Immutable and with no external dependencies.
 */
public final class Password {

    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    /**
     * Creates a Password from a raw string without validation.
     * Use for existing passwords that don't need policy validation.
     */
    public static Password fromRaw(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return new Password(value);
    }

    /**
     * Creates a Password with policy validation.
     *
     * @param value  the password value
     * @param policy the password policy to validate against
     * @return the validated Password
     * @throws PasswordPolicyViolation if the password doesn't meet the policy
     */
    public static Password withPolicy(String value, PasswordPolicy policy) {
        if (value == null || value.isEmpty()) {
            throw new PasswordPolicyViolation("Password cannot be null or empty");
        }

        if (value.length() < policy.minLength()) {
            throw new PasswordPolicyViolation(
                    "Password must be at least " + policy.minLength() + " characters long");
        }

        if (value.length() > policy.maxLength()) {
            throw new PasswordPolicyViolation(
                    "Password must not exceed " + policy.maxLength() + " characters");
        }

        if (policy.requireUppercase() && !HAS_UPPERCASE.matcher(value).find()) {
            throw new PasswordPolicyViolation(
                    "Password must contain at least one uppercase letter");
        }

        if (policy.requireLowercase() && !HAS_LOWERCASE.matcher(value).find()) {
            throw new PasswordPolicyViolation(
                    "Password must contain at least one lowercase letter");
        }

        if (policy.requireDigit() && !HAS_DIGIT.matcher(value).find()) {
            throw new PasswordPolicyViolation(
                    "Password must contain at least one digit");
        }

        if (policy.requireSpecialChar() && !HAS_SPECIAL_CHAR.matcher(value).find()) {
            throw new PasswordPolicyViolation(
                    "Password must contain at least one special character");
        }

        return new Password(value);
    }

    public String value() {
        return value;
    }

    public boolean matches(String other) {
        return value.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Password{***}";
    }

    /**
     * Exception thrown when password doesn't meet policy requirements.
     */
    public static class PasswordPolicyViolation extends RuntimeException {
        public PasswordPolicyViolation(String message) {
            super(message);
        }
    }
}
