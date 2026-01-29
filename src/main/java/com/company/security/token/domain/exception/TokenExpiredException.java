package com.company.security.token.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when a token has expired.
 */
public final class TokenExpiredException extends TokenException {

    public TokenExpiredException() {
        super(ErrorCode.TKN_EXPIRED);
    }

    public TokenExpiredException(String jti) {
        super(ErrorCode.TKN_EXPIRED, "Token expired: " + jti);
    }
}
