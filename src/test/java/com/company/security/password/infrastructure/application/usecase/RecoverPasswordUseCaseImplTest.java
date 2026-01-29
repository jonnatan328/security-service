package com.company.security.password.infrastructure.application.usecase;

import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.password.infrastructure.application.port.output.EventPublisherPort;
import com.company.security.password.infrastructure.application.port.output.PasswordAuditPort;
import com.company.security.password.infrastructure.application.port.output.PasswordResetTokenPort;
import com.company.security.password.infrastructure.application.port.output.UserLookupPort;
import com.company.security.shared.domain.model.Email;
import com.company.security.shared.infrastructure.properties.PasswordPolicyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoverPasswordUseCaseImplTest {

    @Mock
    private UserLookupPort userLookupPort;

    @Mock
    private PasswordResetTokenPort passwordResetTokenPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @Mock
    private PasswordAuditPort passwordAuditPort;

    private PasswordPolicyProperties passwordPolicyProperties;
    private RecoverPasswordUseCaseImpl recoverPasswordUseCase;

    private static final String USER_ID = "user-123";
    private static final String EMAIL = "john.doe@company.com";
    private static final String USERNAME = "john.doe";
    private static final String IP_ADDRESS = "192.168.1.1";
    private static final String USER_AGENT = "TestAgent/1.0";

    @BeforeEach
    void setUp() {
        passwordPolicyProperties = new PasswordPolicyProperties();
        passwordPolicyProperties.setResetTokenExpiration(30);
        passwordPolicyProperties.setResetBaseUrl("http://localhost:3000/reset-password");

        recoverPasswordUseCase = new RecoverPasswordUseCaseImpl(
                userLookupPort,
                passwordResetTokenPort,
                eventPublisherPort,
                passwordAuditPort,
                passwordPolicyProperties);
    }

    @Test
    void recoverPassword_withExistingEmail_publishesEvent() {
        UserLookupPort.UserInfo userInfo = new UserLookupPort.UserInfo(USER_ID, EMAIL, USERNAME);

        when(userLookupPort.findByEmail(EMAIL)).thenReturn(Mono.just(userInfo));
        when(passwordResetTokenPort.cancelAllPendingTokensForUser(USER_ID)).thenReturn(Mono.empty());
        when(passwordResetTokenPort.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(eventPublisherPort.publishPasswordResetRequested(any(PasswordResetToken.class), anyString()))
                .thenReturn(Mono.empty());
        when(passwordAuditPort.recordEvent(
                eq(PasswordAuditPort.EventType.PASSWORD_RESET_REQUESTED),
                eq(USER_ID), eq(EMAIL), eq(true), eq(null), eq(IP_ADDRESS), eq(USER_AGENT)))
                .thenReturn(Mono.empty());

        StepVerifier.create(recoverPasswordUseCase.recoverPassword(EMAIL, IP_ADDRESS, USER_AGENT))
                .verifyComplete();
    }

    @Test
    void recoverPassword_withNonExistingEmail_completesWithoutError() {
        when(userLookupPort.findByEmail("unknown@company.com")).thenReturn(Mono.empty());

        StepVerifier.create(recoverPasswordUseCase.recoverPassword("unknown@company.com", IP_ADDRESS, USER_AGENT))
                .verifyComplete();
    }
}
