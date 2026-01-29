package com.company.security.password.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when a new password matches a recently used password.
 */
public final class PasswordHistoryViolationException extends PasswordException {

    public PasswordHistoryViolationException() {
        super(ErrorCode.PWD_HISTORY_VIOLATION);
    }

    public PasswordHistoryViolationException(int historyCount) {
        super(ErrorCode.PWD_HISTORY_VIOLATION,
                "Password was used within the last " + historyCount + " passwords");
    }
}
