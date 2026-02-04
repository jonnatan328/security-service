package com.company.security.shared.infrastructure.config.security;

import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.port.output.TokenBlacklistPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationManager.class);

    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;

    public JwtAuthenticationManager(TokenProviderPort tokenProviderPort,
                                    TokenBlacklistPort tokenBlacklistPort) {
        this.tokenProviderPort = tokenProviderPort;
        this.tokenBlacklistPort = tokenBlacklistPort;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        return tokenProviderPort.parseAccessToken(token)
                .flatMap(this::checkBlacklist)
                .map(this::createAuthentication)
                .doOnError(e -> log.debug("JWT authentication failed: {}", e.getMessage()));
    }

    private Mono<TokenClaims> checkBlacklist(TokenClaims claims) {
        return tokenBlacklistPort.isBlacklisted(claims.jti())
                .filter(isBlacklisted -> !isBlacklisted)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Token has been revoked")))
                .thenReturn(claims);
    }

    private Authentication createAuthentication(TokenClaims claims) {
        var authorities = claims.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        var principal = new JwtAuthenticatedPrincipal(claims);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );
    }
}
