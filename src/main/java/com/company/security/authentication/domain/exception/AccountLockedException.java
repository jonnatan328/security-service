package com.company.security.authentication.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when authentication fails because the account is locked.
 */
public final class AccountLockedException extends AuthenticationException {

    private final String username;
    private final long lockDurationMinutes;

    public AccountLockedException(String username) {
        super(ErrorCode.AUTH_ACCOUNT_LOCKED);
        this.username = username;
        this.lockDurationMinutes = 0;
    }

    public AccountLockedException(String username, long lockDurationMinutes) {
        super(ErrorCode.AUTH_ACCOUNT_LOCKED,
                "Account locked for " + lockDurationMinutes + " minutes");
        this.username = username;
        this.lockDurationMinutes = lockDurationMinutes;
    }

    public String username() {
        return username;
    }

    public long lockDurationMinutes() {
        return lockDurationMinutes;
    }
}
