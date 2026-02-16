package com.company.security.password.infrastructure.adapter.input.rest.handler;

import com.company.security.password.domain.model.PasswordChangeResult;
import com.company.security.password.domain.port.input.RecoverPasswordUseCase;
import com.company.security.password.domain.port.input.ResetPasswordUseCase;
import com.company.security.password.domain.port.input.UpdatePasswordUseCase;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.RecoverPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.ResetPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.UpdatePasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.response.PasswordOperationResponse;
import com.company.security.password.infrastructure.adapter.input.rest.mapper.PasswordRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordHandlerTest {

    @Mock
    private RecoverPasswordUseCase recoverPasswordUseCase;

    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;

    @Mock
    private UpdatePasswordUseCase updatePasswordUseCase;

    @Mock
    private PasswordRestMapper mapper;

    private PasswordHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PasswordHandler(recoverPasswordUseCase, resetPasswordUseCase, updatePasswordUseCase, mapper);
    }

    @Test
    void recoverPassword_returnsSuccessResponse() {
        RecoverPasswordRequest request = new RecoverPasswordRequest("john@company.com");
        when(recoverPasswordUseCase.recoverPassword("john@company.com", "192.168.1.1", "TestAgent"))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.recoverPassword(request, "192.168.1.1", "TestAgent"))
                .assertNext(response -> {
                    assertThat(response.success()).isTrue();
                    assertThat(response.message()).contains("recovery link");
                })
                .verifyComplete();
    }

    @Test
    void resetPassword_withValidToken_returnsResponse() {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "newPassword123!");
        PasswordChangeResult result = PasswordChangeResult.success("user-123", PasswordChangeResult.ChangeType.RESET);
        PasswordOperationResponse response = new PasswordOperationResponse(true, "Password changed successfully", Instant.now());

        when(resetPasswordUseCase.resetPassword("reset-token", "newPassword123!", "192.168.1.1", "TestAgent"))
                .thenReturn(Mono.just(result));
        when(mapper.toResponse(result)).thenReturn(response);

        StepVerifier.create(handler.resetPassword(request, "192.168.1.1", "TestAgent"))
                .assertNext(resp -> assertThat(resp.success()).isTrue())
                .verifyComplete();
    }

    @Test
    void updatePassword_withValidRequest_returnsResponse() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("oldPass", "newPass123!");
        PasswordChangeResult result = PasswordChangeResult.success("user-123", PasswordChangeResult.ChangeType.UPDATE);
        PasswordOperationResponse response = new PasswordOperationResponse(true, "Password changed successfully", Instant.now());

        when(updatePasswordUseCase.updatePassword("user-123", "oldPass", "newPass123!", "192.168.1.1", "TestAgent"))
                .thenReturn(Mono.just(result));
        when(mapper.toResponse(result)).thenReturn(response);

        StepVerifier.create(handler.updatePassword(request, "user-123", "192.168.1.1", "TestAgent"))
                .assertNext(resp -> assertThat(resp.success()).isTrue())
                .verifyComplete();
    }
}
