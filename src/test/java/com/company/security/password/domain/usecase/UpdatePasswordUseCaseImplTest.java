package com.company.security.password.domain.usecase;

import com.company.security.password.domain.exception.CurrentPasswordMismatchException;
import com.company.security.password.domain.model.PasswordChangeResult;
import com.company.security.password.domain.model.PasswordPolicy;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.password.domain.port.output.DirectoryPasswordPort;
import com.company.security.password.domain.port.output.PasswordAuditPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordUseCaseImplTest {

    @Mock
    private DirectoryPasswordPort directoryPasswordPort;

    @Mock
    private PasswordAuditPort passwordAuditPort;

    private PasswordPolicyService passwordPolicyService;
    private UpdatePasswordUseCaseImpl updatePasswordUseCase;

    private static final String USER_ID = "user-123";
    private static final String CURRENT_PASSWORD = "OldSecure1!";
    private static final String NEW_PASSWORD = "NewSecure1!";
    private static final String IP_ADDRESS = "192.168.1.1";
    private static final String USER_AGENT = "TestAgent/1.0";

    @BeforeEach
    void setUp() {
        passwordPolicyService = new PasswordPolicyService(PasswordPolicy.defaultPolicy());
        updatePasswordUseCase = new UpdatePasswordUseCaseImpl(
                directoryPasswordPort,
                passwordAuditPort,
                passwordPolicyService);
    }

    @Test
    void updatePassword_withCorrectCurrentPassword_changesPassword() {
        when(directoryPasswordPort.verifyPassword(USER_ID, CURRENT_PASSWORD)).thenReturn(Mono.just(true));
        when(directoryPasswordPort.changePassword(USER_ID, NEW_PASSWORD)).thenReturn(Mono.empty());
        when(passwordAuditPort.recordEvent(
                eq(PasswordAuditPort.EventType.PASSWORD_UPDATED),
                eq(USER_ID), eq(null), eq(true), eq(null), eq(IP_ADDRESS), eq(USER_AGENT)))
                .thenReturn(Mono.empty());

        StepVerifier.create(updatePasswordUseCase.updatePassword(USER_ID, CURRENT_PASSWORD, NEW_PASSWORD, IP_ADDRESS, USER_AGENT))
                .assertNext(result -> {
                    assertThat(result.success()).isTrue();
                    assertThat(result.userId()).isEqualTo(USER_ID);
                    assertThat(result.changeType()).isEqualTo(PasswordChangeResult.ChangeType.UPDATE);
                })
                .verifyComplete();
    }

    @Test
    void updatePassword_withWrongCurrentPassword_throwsMismatchException() {
        when(directoryPasswordPort.verifyPassword(USER_ID, CURRENT_PASSWORD)).thenReturn(Mono.just(false));

        StepVerifier.create(updatePasswordUseCase.updatePassword(USER_ID, CURRENT_PASSWORD, NEW_PASSWORD, IP_ADDRESS, USER_AGENT))
                .expectError(CurrentPasswordMismatchException.class)
                .verify();
    }
}
