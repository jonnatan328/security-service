package com.company.security.password.infrastructure.adapter.input.rest.handler;

import com.company.security.password.domain.port.input.RecoverPasswordUseCase;
import com.company.security.password.domain.port.input.ResetPasswordUseCase;
import com.company.security.password.domain.port.input.UpdatePasswordUseCase;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.RecoverPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.ResetPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.UpdatePasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.response.PasswordOperationResponse;
import com.company.security.password.infrastructure.adapter.input.rest.mapper.PasswordRestMapper;
import reactor.core.publisher.Mono;

/**
 * Handler for password REST endpoints.
 * Orchestrates use case calls and response mapping.
 */
public class PasswordHandler {

    private final RecoverPasswordUseCase recoverPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final PasswordRestMapper mapper;

    public PasswordHandler(
            RecoverPasswordUseCase recoverPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            UpdatePasswordUseCase updatePasswordUseCase,
            PasswordRestMapper mapper) {
        this.recoverPasswordUseCase = recoverPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.updatePasswordUseCase = updatePasswordUseCase;
        this.mapper = mapper;
    }

    public Mono<PasswordOperationResponse> recoverPassword(RecoverPasswordRequest request,
                                                            String ipAddress, String userAgent) {
        return recoverPasswordUseCase.recoverPassword(request.email(), ipAddress, userAgent)
                .then(Mono.just(PasswordOperationResponse.success(
                        "If the email exists, a recovery link has been sent")));
    }

    public Mono<PasswordOperationResponse> resetPassword(ResetPasswordRequest request,
                                                          String ipAddress, String userAgent) {
        return resetPasswordUseCase.resetPassword(request.token(), request.newPassword(), ipAddress, userAgent)
                .map(mapper::toResponse);
    }

    public Mono<PasswordOperationResponse> updatePassword(UpdatePasswordRequest request, String userId,
                                                           String ipAddress, String userAgent) {
        return updatePasswordUseCase.updatePassword(
                        userId, request.currentPassword(), request.newPassword(), ipAddress, userAgent)
                .map(mapper::toResponse);
    }
}
