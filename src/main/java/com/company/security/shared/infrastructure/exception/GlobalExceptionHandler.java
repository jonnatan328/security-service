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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String title;
        String errorCode;

        if (ex instanceof InvalidCredentialsException domainEx) {
            status = HttpStatus.UNAUTHORIZED;
            title = "Invalid Credentials";
            errorCode = domainEx.code();
        } else if (ex instanceof AccountLockedException domainEx) {
            status = HttpStatus.valueOf(423);
            title = "Account Locked";
            errorCode = domainEx.code();
        } else if (ex instanceof AccountDisabledException domainEx) {
            status = HttpStatus.FORBIDDEN;
            title = "Account Disabled";
            errorCode = domainEx.code();
        } else if (ex instanceof TokenExpiredException domainEx) {
            status = HttpStatus.UNAUTHORIZED;
            title = "Token Expired";
            errorCode = domainEx.code();
        } else if (ex instanceof InvalidTokenException domainEx) {
            status = HttpStatus.UNAUTHORIZED;
            title = "Invalid Token";
            errorCode = domainEx.code();
        } else if (ex instanceof PasswordValidationException domainEx) {
            status = HttpStatus.BAD_REQUEST;
            title = "Password Validation Failed";
            errorCode = domainEx.code();
        } else if (ex instanceof PasswordResetTokenInvalidException domainEx) {
            status = HttpStatus.BAD_REQUEST;
            title = "Invalid Password Reset Token";
            errorCode = domainEx.code();
        } else if (ex instanceof PasswordResetTokenExpiredException domainEx) {
            status = HttpStatus.BAD_REQUEST;
            title = "Password Reset Token Expired";
            errorCode = domainEx.code();
        } else if (ex instanceof CurrentPasswordMismatchException domainEx) {
            status = HttpStatus.BAD_REQUEST;
            title = "Current Password Mismatch";
            errorCode = domainEx.code();
        } else if (ex instanceof DomainException domainEx) {
            status = HttpStatus.BAD_REQUEST;
            title = "Domain Error";
            errorCode = domainEx.code();
        } else if (ex instanceof ServerWebInputException) {
            status = HttpStatus.BAD_REQUEST;
            title = "Bad Request";
            errorCode = "GEN-901";
        } else {
            log.error("Unexpected error", ex);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            title = "Internal Server Error";
            errorCode = "GEN-900";
        }

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(),
                title,
                ex.getMessage(),
                exchange.getRequest().getPath().value(),
                errorCode
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            return Mono.error(e);
        }
    }
}
