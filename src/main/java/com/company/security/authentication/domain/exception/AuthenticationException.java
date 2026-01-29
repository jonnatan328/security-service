package com.company.security.authentication.domain.exception;

import com.company.security.shared.domain.exception.DomainException;
import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Base exception for authentication-related errors.
 */
public class AuthenticationException extends DomainException {

    protected AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected AuthenticationException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    protected AuthenticationException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    protected AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public static AuthenticationException generic(String details) {
        return new AuthenticationException(ErrorCode.AUTH_INVALID_CREDENTIALS, details);
    }
}
