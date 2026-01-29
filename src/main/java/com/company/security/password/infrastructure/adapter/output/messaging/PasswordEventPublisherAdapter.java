package com.company.security.password.infrastructure.adapter.output.messaging;

import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.password.infrastructure.application.port.output.EventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class PasswordEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(PasswordEventPublisherAdapter.class);
    private static final String TOPIC = "security.password.events";

    private final ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PasswordEventPublisherAdapter(
            ReactiveKafkaProducerTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> publishPasswordResetRequested(PasswordResetToken resetToken, String resetUrl) {
        return Mono.fromCallable(() -> {
            Map<String, Object> event = Map.of(
                    "eventType", "PASSWORD_RESET_REQUESTED",
                    "eventId", UUID.randomUUID().toString(),
                    "timestamp", Instant.now().toString(),
                    "payload", Map.of(
                            "userId", resetToken.userId(),
                            "email", resetToken.email().value(),
                            "resetToken", resetToken.token(),
                            "expiresAt", resetToken.expiresAt().toString(),
                            "resetUrl", resetUrl
                    ),
                    "metadata", Map.of(
                            "correlationId", MDC.get("correlationId") != null ? MDC.get("correlationId") : "",
                            "source", "security-service"
                    )
            );
            return objectMapper.writeValueAsString(event);
        }).flatMap(message -> kafkaTemplate.send(TOPIC, resetToken.userId(), message)
                .doOnSuccess(result -> log.info("Password reset event published for user: {}", resetToken.userId()))
                .doOnError(e -> log.error("Failed to publish password reset event", e))
                .then());
    }
}
