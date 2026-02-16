package com.company.security.password.infrastructure.adapter.output.messaging;

import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.shared.domain.model.Email;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordEventPublisherAdapterTest {

    @Mock
    private ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PasswordEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PasswordEventPublisherAdapter(kafkaTemplate, objectMapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishPasswordResetRequested_sendsKafkaMessage() {
        PasswordResetToken resetToken = buildResetToken();
        SenderResult<Void> senderResult = mock(SenderResult.class);

        when(kafkaTemplate.send(eq("security.password.events"), eq("user-123"), anyString()))
                .thenReturn(Mono.just(senderResult));

        StepVerifier.create(adapter.publishPasswordResetRequested(resetToken, "https://example.com/reset"))
                .verifyComplete();
    }

    private PasswordResetToken buildResetToken() {
        Instant now = Instant.now();
        return PasswordResetToken.builder()
                .id("token-id")
                .token("reset-token-uuid")
                .userId("user-123")
                .email(Email.of("john@company.com"))
                .createdAt(now)
                .expiresAt(now.plusSeconds(3600))
                .status(PasswordResetToken.Status.PENDING)
                .build();
    }
}
