package com.company.security.password.infrastructure.application.usecase;

import com.company.security.password.domain.exception.PasswordResetTokenExpiredException;
import com.company.security.password.domain.exception.PasswordResetTokenInvalidException;
import com.company.security.password.domain.model.PasswordChangeResult;
import com.company.security.password.domain.model.PasswordPolicy;
import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.password.infrastructure.application.port.output.DirectoryPasswordPort;
import com.company.security.password.infrastructure.application.port.output.PasswordAuditPort;
import com.company.security.password.infrastructure.application.port.output.PasswordResetTokenPort;
import com.company.security.shared.domain.model.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseImplTest {

    @Mock
    private PasswordResetTokenPort passwordResetTokenPort;

    @Mock
    private DirectoryPasswordPort directoryPasswordPort;

    @Mock
    private PasswordAuditPort passwordAuditPort;

    private PasswordPolicyService passwordPolicyService;
    private ResetPasswordUseCaseImpl resetPasswordUseCase;

    private static final String USER_ID = "user-123";
    private static final String EMAIL = "john.doe@company.com";
    private static final String TOKEN = "reset-token-value";
    private static final String NEW_PASSWORD = "NewSecure1!";
    private static final String IP_ADDRESS = "192.168.1.1";
    private static final String USER_AGENT = "TestAgent/1.0";

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyService(PasswordPolicy.defaultPolicy());
        resetPasswordUseCase = new ResetPasswordUseCaseImpl(
                passwordResetTokenPort,
                directoryPasswordPort,
                passwordAuditPort,
                passwordPolicyService);
    }

    @Test
    void resetPassword_withValidToken_changesPassword() {
        PasswordResetToken resetToken = buildPendingResetToken();

        when(passwordResetTokenPort.findByToken(TOKEN)).thenReturn(Mono.just(resetToken));
        when(directoryPasswordPort.resetPassword(USER_ID, NEW_PASSWORD)).thenReturn(Mono.empty());
        when(passwordResetTokenPort.markAsUsed(TOKEN)).thenReturn(Mono.just(resetToken.markAsUsed()));
        when(passwordAuditPort.recordEvent(
                eq(PasswordAuditPort.EventType.PASSWORD_RESET_COMPLETED),
                eq(USER_ID), eq(EMAIL), eq(true), eq(null), eq(IP_ADDRESS), eq(USER_AGENT)))
                .thenReturn(Mono.empty());

        StepVerifier.create(resetPasswordUseCase.resetPassword(TOKEN, NEW_PASSWORD, IP_ADDRESS, USER_AGENT))
                .assertNext(result -> {
                    assertThat(result.success()).isTrue();
                    assertThat(result.userId()).isEqualTo(USER_ID);
                    assertThat(result.changeType()).isEqualTo(PasswordChangeResult.ChangeType.RESET);
                })
                .verifyComplete();
    }

    @Test
    void resetPassword_withExpiredToken_throwsException() {
        PasswordResetToken expiredToken = buildExpiredResetToken();

        when(passwordResetTokenPort.findByToken(TOKEN)).thenReturn(Mono.just(expiredToken));

        StepVerifier.create(resetPasswordUseCase.resetPassword(TOKEN, NEW_PASSWORD, IP_ADDRESS, USER_AGENT))
                .expectError(PasswordResetTokenExpiredException.class)
                .verify();
    }

    @Test
    void resetPassword_withInvalidToken_throwsException() {
        when(passwordResetTokenPort.findByToken(TOKEN)).thenReturn(Mono.empty());

        StepVerifier.create(resetPasswordUseCase.resetPassword(TOKEN, NEW_PASSWORD, IP_ADDRESS, USER_AGENT))
                .expectError(PasswordResetTokenInvalidException.class)
                .verify();
    }

    private PasswordResetToken buildPendingResetToken() {
        Instant now = Instant.now();
        return PasswordResetToken.builder()
                .id("reset-id-1")
                .token(TOKEN)
                .userId(USER_ID)
                .email(Email.of(EMAIL))
                .createdAt(now.minusSeconds(60))
                .expiresAt(now.plusSeconds(1800))
                .status(PasswordResetToken.Status.PENDING)
                .build();
    }

    private PasswordResetToken buildExpiredResetToken() {
        Instant now = Instant.now();
        return PasswordResetToken.builder()
                .id("reset-id-2")
                .token(TOKEN)
                .userId(USER_ID)
                .email(Email.of(EMAIL))
                .createdAt(now.minusSeconds(7200))
                .expiresAt(now.minusSeconds(3600))
                .status(PasswordResetToken.Status.PENDING)
                .build();
    }
}
