package com.company.security.token.domain.exception;

import com.company.security.shared.domain.exception.DomainException;
import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Base exception for token-related errors.
 */
public class TokenException extends DomainException {

    protected TokenException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected TokenException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    protected TokenException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }

    protected TokenException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
