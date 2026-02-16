package com.company.security.password.infrastructure.adapter.output.persistence;

import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.password.infrastructure.adapter.output.persistence.document.PasswordResetTokenDocument;
import com.company.security.password.infrastructure.adapter.output.persistence.repository.PasswordResetTokenRepository;
import com.company.security.shared.domain.model.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenMongoAdapterTest {

    @Mock
    private PasswordResetTokenRepository repository;

    private PasswordResetTokenMongoAdapter adapter;

    private static final String TOKEN_ID = "token-id-123";
    private static final String TOKEN_VALUE = "reset-token-uuid";
    private static final String USER_ID = "user-123";
    private static final String EMAIL = "john@company.com";

    @BeforeEach
    void setUp() {
        adapter = new PasswordResetTokenMongoAdapter(repository);
    }

    @Test
    void save_withValidToken_savesAndReturnsDomain() {
        PasswordResetToken domainToken = buildDomainToken();
        PasswordResetTokenDocument savedDoc = buildDocument();

        when(repository.save(any(PasswordResetTokenDocument.class))).thenReturn(Mono.just(savedDoc));

        StepVerifier.create(adapter.save(domainToken))
                .assertNext(result -> {
                    assertThat(result.token()).isEqualTo(TOKEN_VALUE);
                    assertThat(result.userId()).isEqualTo(USER_ID);
                    assertThat(result.email().value()).isEqualTo(EMAIL);
                    assertThat(result.status()).isEqualTo(PasswordResetToken.Status.PENDING);
                })
                .verifyComplete();
    }

    @Test
    void findByToken_withExistingToken_returnsDomain() {
        PasswordResetTokenDocument doc = buildDocument();
        when(repository.findByToken(TOKEN_VALUE)).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.findByToken(TOKEN_VALUE))
                .assertNext(result -> {
                    assertThat(result.token()).isEqualTo(TOKEN_VALUE);
                    assertThat(result.userId()).isEqualTo(USER_ID);
                })
                .verifyComplete();
    }

    @Test
    void findByToken_withNonExistingToken_returnsEmpty() {
        when(repository.findByToken("non-existent")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByToken("non-existent"))
                .verifyComplete();
    }

    @Test
    void cancelAllPendingTokensForUser_cancelsPendingTokens() {
        PasswordResetTokenDocument doc = buildDocument();
        ArgumentCaptor<PasswordResetTokenDocument> captor = ArgumentCaptor.forClass(PasswordResetTokenDocument.class);

        when(repository.findByUserIdAndStatus(USER_ID, "PENDING")).thenReturn(Flux.just(doc));
        when(repository.save(captor.capture())).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.cancelAllPendingTokensForUser(USER_ID))
                .verifyComplete();

        assertThat(captor.getValue().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void markAsUsed_updatesStatusAndUsedAt() {
        PasswordResetTokenDocument doc = buildDocument();
        ArgumentCaptor<PasswordResetTokenDocument> captor = ArgumentCaptor.forClass(PasswordResetTokenDocument.class);

        when(repository.findByToken(TOKEN_VALUE)).thenReturn(Mono.just(doc));
        when(repository.save(captor.capture())).thenReturn(Mono.just(doc));

        StepVerifier.create(adapter.markAsUsed(TOKEN_VALUE))
                .assertNext(result -> assertThat(result).isNotNull())
                .verifyComplete();

        assertThat(captor.getValue().getStatus()).isEqualTo("USED");
        assertThat(captor.getValue().getUsedAt()).isNotNull();
    }

    private PasswordResetToken buildDomainToken() {
        Instant now = Instant.now();
        return PasswordResetToken.builder()
                .id(TOKEN_ID)
                .token(TOKEN_VALUE)
                .userId(USER_ID)
                .email(Email.of(EMAIL))
                .createdAt(now)
                .expiresAt(now.plusSeconds(3600))
                .status(PasswordResetToken.Status.PENDING)
                .build();
    }

    private PasswordResetTokenDocument buildDocument() {
        Instant now = Instant.now();
        PasswordResetTokenDocument doc = new PasswordResetTokenDocument();
        doc.setId(TOKEN_ID);
        doc.setToken(TOKEN_VALUE);
        doc.setUserId(USER_ID);
        doc.setEmail(EMAIL);
        doc.setCreatedAt(now);
        doc.setExpiresAt(now.plusSeconds(3600));
        doc.setStatus("PENDING");
        return doc;
    }
}
