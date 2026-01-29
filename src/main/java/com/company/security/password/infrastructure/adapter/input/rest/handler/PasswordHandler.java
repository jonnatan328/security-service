package com.company.security.password.infrastructure.adapter.input.rest.handler;

import com.company.security.password.infrastructure.adapter.input.rest.dto.request.RecoverPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.ResetPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.UpdatePasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.response.PasswordOperationResponse;
import com.company.security.password.infrastructure.adapter.input.rest.mapper.PasswordRestMapper;
import com.company.security.password.infrastructure.application.port.input.RecoverPasswordUseCase;
import com.company.security.password.infrastructure.application.port.input.ResetPasswordUseCase;
import com.company.security.password.infrastructure.application.port.input.UpdatePasswordUseCase;
import jakarta.validation.Validator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Optional;

@Component
public class PasswordHandler {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final RecoverPasswordUseCase recoverPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final PasswordRestMapper mapper;
    private final Validator validator;

    public PasswordHandler(
            RecoverPasswordUseCase recoverPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            UpdatePasswordUseCase updatePasswordUseCase,
            PasswordRestMapper mapper,
            Validator validator) {
        this.recoverPasswordUseCase = recoverPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.updatePasswordUseCase = updatePasswordUseCase;
        this.mapper = mapper;
        this.validator = validator;
    }

    public Mono<ServerResponse> recoverPassword(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = request.headers().firstHeader(HttpHeaders.USER_AGENT);

        return request.bodyToMono(RecoverPasswordRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> recoverPasswordUseCase.recoverPassword(req.email(), ipAddress, userAgent))
                .then(ServerResponse.accepted()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(PasswordOperationResponse.success(
                                "If the email exists, a recovery link has been sent")));
    }

    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = request.headers().firstHeader(HttpHeaders.USER_AGENT);

        return request.bodyToMono(ResetPasswordRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> resetPasswordUseCase.resetPassword(
                        req.token(), req.newPassword(), ipAddress, userAgent))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> updatePassword(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = request.headers().firstHeader(HttpHeaders.USER_AGENT);

        return request.principal()
                .map(Principal::getName)
                .flatMap(userId -> request.bodyToMono(UpdatePasswordRequest.class)
                        .flatMap(this::validate)
                        .flatMap(req -> updatePasswordUseCase.updatePassword(
                                userId, req.currentPassword(), req.newPassword(),
                                ipAddress, userAgent)))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    private String extractIpAddress(ServerRequest request) {
        return Optional.ofNullable(request.headers().firstHeader(X_FORWARDED_FOR))
                .map(header -> header.split(",")[0].trim())
                .orElseGet(() -> request.remoteAddress()
                        .map(address -> address.getAddress().getHostAddress())
                        .orElse("unknown"));
    }

    private <T> Mono<T> validate(T object) {
        Errors errors = new BeanPropertyBindingResult(object, object.getClass().getSimpleName());
        validator.validate(object).forEach(violation ->
            errors.rejectValue(
                violation.getPropertyPath().toString(),
                "",
                violation.getMessage()
            )
        );
        if (errors.hasErrors()) {
            return Mono.error(new ServerWebInputException(errors.getAllErrors().toString()));
        }
        return Mono.just(object);
    }
}
