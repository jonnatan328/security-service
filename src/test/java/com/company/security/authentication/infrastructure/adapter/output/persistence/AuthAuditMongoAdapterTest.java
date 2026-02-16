package com.company.security.authentication.infrastructure.adapter.output.persistence;

import com.company.security.authentication.infrastructure.adapter.output.persistence.document.AuthAuditDocument;
import com.company.security.authentication.infrastructure.adapter.output.persistence.repository.AuthAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthAuditMongoAdapterTest {

    @Mock
    private AuthAuditRepository repository;

    private AuthAuditMongoAdapter adapter;

    private static final String USER_ID = "user-123";
    private static final String USERNAME = "john.doe";
    private static final String IP = "192.168.1.1";
    private static final String UA = "TestAgent/1.0";

    @BeforeEach
    void setUp() {
        adapter = new AuthAuditMongoAdapter(repository);
    }

    @Test
    void recordSignInSuccess_savesDocument() {
        ArgumentCaptor<AuthAuditDocument> captor = ArgumentCaptor.forClass(AuthAuditDocument.class);
        when(repository.save(captor.capture())).thenReturn(Mono.just(new AuthAuditDocument("SIGN_IN_SUCCESS", USER_ID, USERNAME, true, null, null)));

        StepVerifier.create(adapter.recordSignInSuccess(USER_ID, USERNAME, IP, UA))
                .verifyComplete();

        AuthAuditDocument saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo("SIGN_IN_SUCCESS");
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getUsername()).isEqualTo(USERNAME);
        assertThat(saved.isSuccess()).isTrue();
    }

    @Test
    void recordSignInFailure_savesDocumentWithFailureReason() {
        when(repository.save(any(AuthAuditDocument.class))).thenReturn(
                Mono.just(new AuthAuditDocument("SIGN_IN_FAILED", null, USERNAME, false, "Invalid credentials", null)));

        StepVerifier.create(adapter.recordSignInFailure(USERNAME, IP, UA, "Invalid credentials"))
                .verifyComplete();
    }

    @Test
    void recordSignOut_savesDocument() {
        when(repository.save(any(AuthAuditDocument.class))).thenReturn(
                Mono.just(new AuthAuditDocument("SIGN_OUT", USER_ID, USERNAME, true, null, null)));

        StepVerifier.create(adapter.recordSignOut(USER_ID, USERNAME, IP, UA))
                .verifyComplete();
    }

    @Test
    void recordTokenRefresh_savesDocument() {
        when(repository.save(any(AuthAuditDocument.class))).thenReturn(
                Mono.just(new AuthAuditDocument("TOKEN_REFRESH", USER_ID, USERNAME, true, null, null)));

        StepVerifier.create(adapter.recordTokenRefresh(USER_ID, USERNAME, IP, UA))
                .verifyComplete();
    }

    @Test
    void recordTokenRevoked_savesDocument() {
        when(repository.save(any(AuthAuditDocument.class))).thenReturn(
                Mono.just(new AuthAuditDocument("TOKEN_REVOKED", USER_ID, USERNAME, true, null, null)));

        StepVerifier.create(adapter.recordTokenRevoked(USER_ID, USERNAME, IP, UA))
                .verifyComplete();
    }
}
