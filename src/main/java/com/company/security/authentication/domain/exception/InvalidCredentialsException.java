package com.company.security.authentication.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 */
public final class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException() {
        super(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException(String username) {
        super(ErrorCode.AUTH_INVALID_CREDENTIALS, "Authentication failed for user: " + username);
    }
}
