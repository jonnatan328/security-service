package com.company.security.shared.infrastructure.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class SecurityContextRepository implements ServerSecurityContextRepository {

    private static final Logger log = LoggerFactory.getLogger(SecurityContextRepository.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_ERROR_ATTR = "AUTH_EXCEPTION";

    private final JwtAuthenticationManager authenticationManager;

    public SecurityContextRepository(JwtAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.empty();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(token, token))
                .map(auth -> (SecurityContext) new SecurityContextImpl(auth))
                .onErrorResume(AuthenticationException.class, e -> {
                    log.debug("Authentication failed: {}", e.getMessage());
                    exchange.getAttributes().put(AUTH_ERROR_ATTR, e);
                    return Mono.empty();
                });
    }
}
