package com.company.security.token.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when a token has been revoked.
 */
public final class TokenRevokedException extends TokenException {

    public TokenRevokedException() {
        super(ErrorCode.TKN_REVOKED);
    }

    public TokenRevokedException(String jti) {
        super(ErrorCode.TKN_REVOKED, "Token revoked: " + jti);
    }
}
