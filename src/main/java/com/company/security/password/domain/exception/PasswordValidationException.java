package com.company.security.password.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when a password doesn't meet policy requirements.
 */
public final class PasswordValidationException extends PasswordException {

    private final List<String> violations;

    public PasswordValidationException(String violation) {
        super(ErrorCode.PWD_VALIDATION_FAILED, violation);
        this.violations = Collections.singletonList(violation);
    }

    public PasswordValidationException(List<String> violations) {
        super(ErrorCode.PWD_VALIDATION_FAILED, String.join("; ", violations));
        this.violations = Collections.unmodifiableList(violations);
    }

    public List<String> violations() {
        return violations;
    }
}
