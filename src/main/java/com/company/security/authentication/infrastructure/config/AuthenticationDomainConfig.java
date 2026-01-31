package com.company.security.authentication.infrastructure.config;

import com.company.security.authentication.domain.port.input.RefreshTokenUseCase;
import com.company.security.authentication.domain.port.input.SignInUseCase;
import com.company.security.authentication.domain.port.input.SignOutUseCase;
import com.company.security.authentication.domain.port.output.*;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.domain.usecase.RefreshTokenUseCaseImpl;
import com.company.security.authentication.domain.usecase.SignInUseCaseImpl;
import com.company.security.authentication.domain.usecase.SignOutUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain bean configuration for authentication feature.
 * Wires domain services and use cases.
 */
@Configuration
public class AuthenticationDomainConfig {

    @Bean
    public AuthenticationDomainService authenticationDomainService() {
        return new AuthenticationDomainService();
    }

    @Bean
    public SignInUseCase signInUseCase(
            DirectoryServicePort directoryServicePort,
            TokenProviderPort tokenProviderPort,
            RefreshTokenPort refreshTokenPort,
            AuthAuditPort authAuditPort,
            AuthenticationDomainService authenticationDomainService) {
        return new SignInUseCaseImpl(directoryServicePort, tokenProviderPort, refreshTokenPort,
                authAuditPort, authenticationDomainService);
    }

    @Bean
    public SignOutUseCase signOutUseCase(
            TokenProviderPort tokenProviderPort,
            TokenBlacklistPort tokenBlacklistPort,
            RefreshTokenPort refreshTokenPort,
            AuthAuditPort authAuditPort) {
        return new SignOutUseCaseImpl(tokenProviderPort, tokenBlacklistPort, refreshTokenPort, authAuditPort);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            TokenProviderPort tokenProviderPort,
            TokenBlacklistPort tokenBlacklistPort,
            RefreshTokenPort refreshTokenPort,
            DirectoryServicePort directoryServicePort,
            AuthAuditPort authAuditPort,
            AuthenticationDomainService authenticationDomainService) {
        return new RefreshTokenUseCaseImpl(tokenProviderPort, tokenBlacklistPort, refreshTokenPort,
                directoryServicePort, authAuditPort, authenticationDomainService);
    }
}
