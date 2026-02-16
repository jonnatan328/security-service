package com.company.security.password.domain.service;

import com.company.security.password.domain.exception.PasswordValidationException;
import com.company.security.password.domain.model.Password;
import com.company.security.password.domain.model.PasswordPolicy;
import com.company.security.password.domain.model.PasswordResetToken;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Domain service containing password policy business rules.
 * Pure domain logic with no external dependencies.
 */
public record PasswordPolicyService(PasswordPolicy policy) {

    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public PasswordPolicyService(PasswordPolicy policy) {
        this.policy = policy != null ? policy : PasswordPolicy.defaultPolicy();
    }

    /**
     * Validates a password against the policy.
     *
     * @param rawPassword the raw password string to validate
     * @throws PasswordValidationException if the password doesn't meet the policy
     */
    public void validatePassword(String rawPassword) {
        List<String> violations = new ArrayList<>();

        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new PasswordValidationException("Password cannot be empty");
        }

        if (rawPassword.length() < policy.minLength()) {
            violations.add("Password must be at least " + policy.minLength() + " characters");
        }

        if (rawPassword.length() > policy.maxLength()) {
            violations.add("Password must not exceed " + policy.maxLength() + " characters");
        }

        if (policy.requireUppercase() && !HAS_UPPERCASE.matcher(rawPassword).find()) {
            violations.add("Password must contain at least one uppercase letter");
        }

        if (policy.requireLowercase() && !HAS_LOWERCASE.matcher(rawPassword).find()) {
            violations.add("Password must contain at least one lowercase letter");
        }

        if (policy.requireDigit() && !HAS_DIGIT.matcher(rawPassword).find()) {
            violations.add("Password must contain at least one digit");
        }

        if (policy.requireSpecialChar() && !HAS_SPECIAL_CHAR.matcher(rawPassword).find()) {
            violations.add("Password must contain at least one special character");
        }

        if (!violations.isEmpty()) {
            throw new PasswordValidationException(violations);
        }
    }

    /**
     * Creates a Password value object after validating against the policy.
     *
     * @param rawPassword the raw password string
     * @return the validated Password value object
     * @throws PasswordValidationException if validation fails
     */
    public Password createValidatedPassword(String rawPassword) {
        validatePassword(rawPassword);
        return Password.fromRaw(rawPassword);
    }

    /**
     * Validates that a password reset token is valid for use.
     *
     * @param token the password reset token to validate
     * @return true if the token is valid
     */
    public boolean isResetTokenValid(PasswordResetToken token) {
        return token != null && token.isValid();
    }

}
