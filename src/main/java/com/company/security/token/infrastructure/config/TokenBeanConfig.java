package com.company.security.token.infrastructure.config;

import com.company.security.authentication.infrastructure.adapter.output.token.JwtTokenProviderAdapter;
import com.company.security.authentication.infrastructure.adapter.output.token.TokenBlacklistRedisAdapter;
import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.token.domain.model.Token;
import com.company.security.token.infrastructure.application.port.output.TokenBlacklistCheckPort;
import com.company.security.token.infrastructure.application.port.output.TokenIntrospectionPort;
import com.company.security.authentication.infrastructure.application.port.output.TokenProviderPort;
import com.company.security.authentication.infrastructure.application.port.output.TokenBlacklistPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Bean configuration for token feature.
 * Wires token feature ports to shared infrastructure adapters.
 */
@Configuration
public class TokenBeanConfig {

    @Bean
    public TokenIntrospectionPort tokenIntrospectionPort(TokenProviderPort tokenProviderPort) {
        return rawToken -> tokenProviderPort.parseAccessToken(rawToken)
                .map(claims -> Token.builder()
                        .rawToken(rawToken)
                        .jti(claims.jti())
                        .subject(claims.subject())
                        .userId(claims.userId())
                        .email(claims.email())
                        .roles(claims.roles())
                        .issuedAt(claims.issuedAt())
                        .expiresAt(claims.expiresAt())
                        .issuer(claims.issuer())
                        .build());
    }

    @Bean
    public TokenBlacklistCheckPort tokenBlacklistCheckPort(TokenBlacklistPort tokenBlacklistPort) {
        return tokenBlacklistPort::isBlacklisted;
    }
}
