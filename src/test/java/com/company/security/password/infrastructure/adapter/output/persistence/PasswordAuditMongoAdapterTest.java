package com.company.security.password.infrastructure.adapter.output.persistence;

import com.company.security.password.domain.port.output.PasswordAuditPort;
import com.company.security.password.infrastructure.adapter.output.persistence.document.PasswordAuditDocument;
import com.company.security.password.infrastructure.adapter.output.persistence.repository.PasswordAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordAuditMongoAdapterTest {

    @Mock
    private PasswordAuditRepository repository;

    private PasswordAuditMongoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PasswordAuditMongoAdapter(repository);
    }

    @Test
    void recordEvent_savesDocumentSuccessfully() {
        when(repository.save(any(PasswordAuditDocument.class)))
                .thenReturn(Mono.just(new PasswordAuditDocument("PASSWORD_RESET_REQUESTED", "user-123", "john@company.com", true, null, null)));

        StepVerifier.create(adapter.recordEvent(
                        PasswordAuditPort.EventType.PASSWORD_RESET_REQUESTED,
                        "user-123", "john@company.com", true, null, "192.168.1.1", "TestAgent"))
                .verifyComplete();
    }

    @Test
    void recordEvent_withFailure_savesWithFailureReason() {
        when(repository.save(any(PasswordAuditDocument.class)))
                .thenReturn(Mono.just(new PasswordAuditDocument("PASSWORD_RESET_FAILED", "user-123", "john@company.com", false, "Token expired", null)));

        StepVerifier.create(adapter.recordEvent(
                        PasswordAuditPort.EventType.PASSWORD_RESET_FAILED,
                        "user-123", "john@company.com", false, "Token expired", "192.168.1.1", "TestAgent"))
                .verifyComplete();
    }
}
