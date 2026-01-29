package com.company.security.token.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when a token is invalid.
 */
public final class InvalidTokenException extends TokenException {

    public InvalidTokenException() {
        super(ErrorCode.TKN_INVALID);
    }

    public InvalidTokenException(String details) {
        super(ErrorCode.TKN_INVALID, details);
    }

    public InvalidTokenException(String details, Throwable cause) {
        super(ErrorCode.TKN_INVALID, details, cause);
    }
}
