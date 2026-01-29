package com.company.security.password.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when the current password doesn't match during password update.
 */
public final class CurrentPasswordMismatchException extends PasswordException {

    public CurrentPasswordMismatchException() {
        super(ErrorCode.PWD_CURRENT_MISMATCH);
    }

    public CurrentPasswordMismatchException(String userId) {
        super(ErrorCode.PWD_CURRENT_MISMATCH, "Current password mismatch for user: " + userId);
    }
}
