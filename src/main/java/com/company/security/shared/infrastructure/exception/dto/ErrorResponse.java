package com.company.security.shared.infrastructure.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * RFC 7807 Problem Details error response record.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String errorCode,
        Instant timestamp,
        Map<String, String> fieldErrors
) {

    public static ErrorResponse of(int status, String title, String detail, String instance, String errorCode) {
        return new ErrorResponse(
                "about:blank",
                title,
                status,
                detail,
                instance,
                errorCode,
                Instant.now(),
                null
        );
    }

    public static ErrorResponse ofValidation(int status, String title, String detail,
                                             String instance, String errorCode,
                                             Map<String, String> fieldErrors) {
        return new ErrorResponse(
                "about:blank",
                title,
                status,
                detail,
                instance,
                errorCode,
                Instant.now(),
                fieldErrors
        );
    }
}
