package com.company.security.password.infrastructure.application.port.output;

import com.company.security.password.domain.model.PasswordResetToken;
import reactor.core.publisher.Mono;

/**
 * Output port for publishing domain events.
 * Publishes events to a message broker for other services to consume.
 */
public interface EventPublisherPort {

    Mono<Void> publishPasswordResetRequested(PasswordResetToken resetToken, String resetUrl);
}
