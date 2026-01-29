package com.company.security.shared.domain.exception;

/**
 * Base exception for all domain-level exceptions.
 * No external dependencies - pure domain exception.
 */
public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    protected DomainException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    protected DomainException(ErrorCode errorCode, String details) {
        super(errorCode.defaultMessage() + (details != null ? ": " + details : ""));
        this.errorCode = errorCode;
        this.details = details;
    }

    protected DomainException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.defaultMessage() + (details != null ? ": " + details : ""), cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    protected DomainException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.defaultMessage(), cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String code() {
        return errorCode.code();
    }

    public String details() {
        return details;
    }
}
