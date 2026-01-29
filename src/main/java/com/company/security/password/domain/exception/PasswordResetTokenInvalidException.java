package com.company.security.password.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when a password reset token is invalid or not found.
 */
public final class PasswordResetTokenInvalidException extends PasswordException {

    public PasswordResetTokenInvalidException() {
        super(ErrorCode.PWD_RESET_TOKEN_INVALID);
    }

    public PasswordResetTokenInvalidException(String details) {
        super(ErrorCode.PWD_RESET_TOKEN_INVALID, details);
    }
}
