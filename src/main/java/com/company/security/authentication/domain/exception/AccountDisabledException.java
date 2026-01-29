package com.company.security.authentication.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when authentication fails because the account is disabled.
 */
public final class AccountDisabledException extends AuthenticationException {

    private final String username;

    public AccountDisabledException(String username) {
        super(ErrorCode.AUTH_ACCOUNT_DISABLED, "Account disabled for user: " + username);
        this.username = username;
    }

    public String username() {
        return username;
    }
}
