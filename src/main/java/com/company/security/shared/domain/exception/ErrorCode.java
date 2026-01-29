package com.company.security.shared.domain.exception;

/**
 * Centralized error codes for the Security Service domain.
 * No external dependencies - pure domain enum.
 */
public enum ErrorCode {

    // Authentication errors (AUTH-1XX)
    AUTH_INVALID_CREDENTIALS("AUTH-100", "Invalid username or password"),
    AUTH_ACCOUNT_LOCKED("AUTH-101", "Account is locked due to too many failed attempts"),
    AUTH_ACCOUNT_DISABLED("AUTH-102", "Account is disabled"),
    AUTH_ACCOUNT_EXPIRED("AUTH-103", "Account has expired"),
    AUTH_DIRECTORY_SERVICE_ERROR("AUTH-104", "Directory service is unavailable"),
    AUTH_INVALID_TOKEN("AUTH-105", "Invalid or expired token"),
    AUTH_TOKEN_REVOKED("AUTH-106", "Token has been revoked"),
    AUTH_REFRESH_TOKEN_INVALID("AUTH-107", "Invalid refresh token"),
    AUTH_REFRESH_TOKEN_EXPIRED("AUTH-108", "Refresh token has expired"),

    // Password errors (PWD-2XX)
    PWD_VALIDATION_FAILED("PWD-200", "Password does not meet policy requirements"),
    PWD_RESET_TOKEN_INVALID("PWD-201", "Invalid password reset token"),
    PWD_RESET_TOKEN_EXPIRED("PWD-202", "Password reset token has expired"),
    PWD_RESET_TOKEN_USED("PWD-203", "Password reset token has already been used"),
    PWD_CURRENT_MISMATCH("PWD-204", "Current password is incorrect"),
    PWD_HISTORY_VIOLATION("PWD-205", "Password was used recently"),
    PWD_USER_NOT_FOUND("PWD-206", "User not found"),

    // Token errors (TKN-3XX)
    TKN_INVALID("TKN-300", "Invalid token"),
    TKN_EXPIRED("TKN-301", "Token has expired"),
    TKN_REVOKED("TKN-302", "Token has been revoked"),
    TKN_MALFORMED("TKN-303", "Token is malformed"),
    TKN_SIGNATURE_INVALID("TKN-304", "Token signature is invalid"),

    // General errors (GEN-9XX)
    GEN_INTERNAL_ERROR("GEN-900", "Internal server error"),
    GEN_VALIDATION_ERROR("GEN-901", "Validation error"),
    GEN_RATE_LIMIT_EXCEEDED("GEN-902", "Rate limit exceeded"),
    GEN_SERVICE_UNAVAILABLE("GEN-903", "Service temporarily unavailable");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
