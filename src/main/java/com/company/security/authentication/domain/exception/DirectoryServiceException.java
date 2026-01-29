package com.company.security.authentication.domain.exception;

import com.company.security.shared.domain.exception.ErrorCode;

/**
 * Exception thrown when the directory service (LDAP/AD) is unavailable or returns an error.
 */
public final class DirectoryServiceException extends AuthenticationException {

    public DirectoryServiceException() {
        super(ErrorCode.AUTH_DIRECTORY_SERVICE_ERROR);
    }

    public DirectoryServiceException(String details) {
        super(ErrorCode.AUTH_DIRECTORY_SERVICE_ERROR, details);
    }

    public DirectoryServiceException(String details, Throwable cause) {
        super(ErrorCode.AUTH_DIRECTORY_SERVICE_ERROR, details, cause);
    }

    public DirectoryServiceException(Throwable cause) {
        super(ErrorCode.AUTH_DIRECTORY_SERVICE_ERROR, cause);
    }
}
