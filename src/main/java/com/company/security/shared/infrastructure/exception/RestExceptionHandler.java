package com.company.security.shared.infrastructure.exception;

import com.company.security.authentication.domain.exception.AccountDisabledException;
import com.company.security.authentication.domain.exception.AccountLockedException;
import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.password.domain.exception.CurrentPasswordMismatchException;
import com.company.security.password.domain.exception.PasswordResetTokenExpiredException;
import com.company.security.password.domain.exception.PasswordResetTokenInvalidException;
import com.company.security.password.domain.exception.PasswordValidationException;
import com.company.security.shared.domain.exception.DomainException;
import com.company.security.shared.infrastructure.exception.dto.ErrorResponse;
import com.company.security.token.domain.exception.InvalidTokenException;
import com.company.security.token.domain.exception.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidation(WebExchangeBindException ex, ServerHttpRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse response = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "One or more fields have validation errors",
                request.getPath().value(),
                "GEN-901",
                fieldErrors
        );

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(response);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(ServerWebInputException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, "GEN-901");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.valueOf(423), "Account Locked", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabled(AccountDisabledException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Account Disabled", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token Expired", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid Token", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<ErrorResponse> handlePasswordValidation(PasswordValidationException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Password Validation Failed", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(PasswordResetTokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenInvalid(PasswordResetTokenInvalidException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Password Reset Token", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(PasswordResetTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenExpired(PasswordResetTokenExpiredException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Password Reset Token Expired", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(CurrentPasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrentPasswordMismatch(CurrentPasswordMismatchException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Current Password Mismatch", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", ex.getMessage(), request, "GEN-902");
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex, ServerHttpRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Domain Error", ex.getMessage(), request, ex.code());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, ServerHttpRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.", request, "GEN-900");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String title, String detail,
                                                         ServerHttpRequest request, String errorCode) {
        ErrorResponse response = ErrorResponse.of(
                status.value(), title, detail, request.getPath().value(), errorCode);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(response);
    }
}
