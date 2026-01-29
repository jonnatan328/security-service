package com.company.security.shared.infrastructure.config.security;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Placeholder JWT authentication WebFilter.
 * Authentication is handled via {@link SecurityContextRepository} approach,
 * so this filter is intentionally minimal.
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange);
    }
}
