package com.company.security.shared.infrastructure.exception.dto;

import java.time.Instant;

/**
 * RFC 7807 Problem Details error response record.
 */
public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String errorCode,
        Instant timestamp
) {

    public static ErrorResponse of(int status, String title, String detail, String instance, String errorCode) {
        return new ErrorResponse(
                "about:blank",
                title,
                status,
                detail,
                instance,
                errorCode,
                Instant.now()
        );
    }
}
