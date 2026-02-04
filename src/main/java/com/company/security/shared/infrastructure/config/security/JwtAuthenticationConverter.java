package com.company.security.shared.infrastructure.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Placeholder ServerAuthenticationConverter.
 * Authentication conversion is handled via {@link SecurityContextRepository},
 * so this converter is intentionally minimal.
 */
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.empty();
    }
}
