package com.company.security.password.infrastructure.adapter.input.rest.controller;

import com.company.security.password.infrastructure.adapter.input.rest.dto.request.RecoverPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.ResetPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.UpdatePasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.response.PasswordOperationResponse;
import com.company.security.password.infrastructure.adapter.input.rest.handler.PasswordHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Principal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    @Mock
    private PasswordHandler handler;

    private PasswordController controller;

    @BeforeEach
    void setUp() {
        controller = new PasswordController(handler);
    }

    @Test
    void recoverPassword_returns202() {
        RecoverPasswordRequest request = new RecoverPasswordRequest("john@company.com");
        PasswordOperationResponse response = PasswordOperationResponse.success("Recovery link sent");
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/password/recover").build();

        when(handler.recoverPassword(eq(request), anyString(), any()))
                .thenReturn(Mono.just(response));

        StepVerifier.create(controller.recoverPassword(request, httpRequest))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().success()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void resetPassword_returns200() {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "newPassword123!");
        PasswordOperationResponse response = new PasswordOperationResponse(true, "Password reset", Instant.now());
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/password/reset").build();

        when(handler.resetPassword(eq(request), anyString(), any()))
                .thenReturn(Mono.just(response));

        StepVerifier.create(controller.resetPassword(request, httpRequest))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getBody().success()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void updatePassword_returns200() {
        UpdatePasswordRequest request = new UpdatePasswordRequest("oldPass", "newPass123!");
        PasswordOperationResponse response = new PasswordOperationResponse(true, "Password updated", Instant.now());
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/password/update").build();
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("user-123");

        when(handler.updatePassword(eq(request), eq("user-123"), anyString(), any()))
                .thenReturn(Mono.just(response));

        StepVerifier.create(controller.updatePassword(request, principal, httpRequest))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getBody().success()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void recoverPassword_withXForwardedFor_extractsIp() {
        RecoverPasswordRequest request = new RecoverPasswordRequest("john@company.com");
        PasswordOperationResponse response = PasswordOperationResponse.success("sent");
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/password/recover")
                .header("X-Forwarded-For", "10.0.0.1, 192.168.1.1")
                .build();

        when(handler.recoverPassword(eq(request), eq("10.0.0.1"), any()))
                .thenReturn(Mono.just(response));

        StepVerifier.create(controller.recoverPassword(request, httpRequest))
                .assertNext(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED))
                .verifyComplete();
    }
}
