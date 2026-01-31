package com.company.security.token.infrastructure.config;

import com.company.security.token.domain.port.input.ValidateTokenUseCase;
import com.company.security.token.domain.port.output.TokenBlacklistCheckPort;
import com.company.security.token.domain.port.output.TokenIntrospectionPort;
import com.company.security.token.domain.usecase.ValidateTokenUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain bean configuration for token feature.
 * Wires use cases.
 */
@Configuration
public class TokenDomainConfig {

    @Bean
    public ValidateTokenUseCase validateTokenUseCase(
            TokenIntrospectionPort tokenIntrospectionPort,
            TokenBlacklistCheckPort tokenBlacklistCheckPort) {
        return new ValidateTokenUseCaseImpl(tokenIntrospectionPort, tokenBlacklistCheckPort);
    }
}
