package com.company.security.token.infrastructure.config;

import com.company.security.authentication.domain.port.output.TokenBlacklistPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
import com.company.security.token.domain.model.Token;
import com.company.security.token.domain.port.input.ValidateTokenUseCase;
import com.company.security.token.domain.port.output.TokenBlacklistCheckPort;
import com.company.security.token.domain.port.output.TokenIntrospectionPort;
import com.company.security.token.domain.usecase.ValidateTokenUseCaseImpl;
import com.company.security.token.infrastructure.adapter.input.rest.handler.TokenValidationHandler;
import com.company.security.token.infrastructure.adapter.input.rest.mapper.TokenRestMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public ValidateTokenUseCase validateTokenUseCase(
            TokenIntrospectionPort tokenIntrospectionPort,
            TokenBlacklistCheckPort tokenBlacklistCheckPort) {
        return new ValidateTokenUseCaseImpl(tokenIntrospectionPort, tokenBlacklistCheckPort);
    }

    @Bean
    public TokenRestMapper tokenRestMapper() {
        return new TokenRestMapper();
    }

    @Bean
    public TokenValidationHandler tokenValidationHandler(
            ValidateTokenUseCase validateTokenUseCase,
            TokenRestMapper tokenRestMapper) {
        return new TokenValidationHandler(validateTokenUseCase, tokenRestMapper);
    }
}
