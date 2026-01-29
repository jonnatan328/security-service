package com.company.security.password.domain.exception;

import com.company.security.shared.domain.exception.DomainException;
import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Base exception for password-related errors.
 */
public class PasswordException extends DomainException {

    protected PasswordException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected PasswordException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    protected PasswordException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    protected PasswordException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
