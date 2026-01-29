package com.company.security.password.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when a password reset token has expired.
 */
public final class PasswordResetTokenExpiredException extends PasswordException {

    public PasswordResetTokenExpiredException() {
        super(ErrorCode.PWD_RESET_TOKEN_EXPIRED);
    }

    public PasswordResetTokenExpiredException(String token) {
        super(ErrorCode.PWD_RESET_TOKEN_EXPIRED, "Token expired: " + maskToken(token));
    }

    private static String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}
