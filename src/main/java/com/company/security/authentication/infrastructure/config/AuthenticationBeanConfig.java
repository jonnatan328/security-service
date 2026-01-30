package com.company.security.authentication.infrastructure.config;

import com.company.security.authentication.domain.port.input.RefreshTokenUseCase;
import com.company.security.authentication.domain.port.input.SignInUseCase;
import com.company.security.authentication.domain.port.input.SignOutUseCase;
import com.company.security.authentication.domain.port.output.*;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.domain.usecase.RefreshTokenUseCaseImpl;
import com.company.security.authentication.domain.usecase.SignInUseCaseImpl;
import com.company.security.authentication.domain.usecase.SignOutUseCaseImpl;
import com.company.security.authentication.infrastructure.adapter.input.rest.handler.AuthenticationHandler;
import com.company.security.authentication.infrastructure.adapter.input.rest.mapper.AuthenticationRestMapper;
import com.company.security.authentication.infrastructure.adapter.output.directory.ActiveDirectoryAdapter;
import com.company.security.authentication.infrastructure.adapter.output.directory.DirectoryUserMapper;
import com.company.security.authentication.infrastructure.adapter.output.directory.LdapDirectoryAdapter;
import com.company.security.authentication.infrastructure.adapter.output.persistence.AuthAuditMongoAdapter;
import com.company.security.authentication.infrastructure.adapter.output.persistence.repository.AuthAuditRepository;
import com.company.security.authentication.infrastructure.adapter.output.token.JwtTokenProviderAdapter;
import com.company.security.authentication.infrastructure.adapter.output.token.RefreshTokenRedisAdapter;
import com.company.security.authentication.infrastructure.adapter.output.token.TokenBlacklistRedisAdapter;
import com.company.security.shared.infrastructure.properties.ActiveDirectoryProperties;
import com.company.security.shared.infrastructure.properties.JwtProperties;
import com.company.security.shared.infrastructure.properties.LdapProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

/**
 * Bean configuration for authentication feature.
 */
@Configuration
public class AuthenticationBeanConfig {

    @Bean
    public AuthenticationDomainService authenticationDomainService() {
        return new AuthenticationDomainService();
    }

    @Bean
    public DirectoryUserMapper directoryUserMapper() {
        return new DirectoryUserMapper();
    }

    @Bean
    @ConditionalOnProperty(name = "ldap.active-directory.enabled", havingValue = "false", matchIfMissing = true)
    public LdapDirectoryAdapter ldapDirectoryAdapter(
            LdapTemplate ldapTemplate,
            LdapContextSource ldapContextSource,
            DirectoryUserMapper directoryUserMapper,
            LdapProperties ldapProperties) {
        return new LdapDirectoryAdapter(ldapTemplate, ldapContextSource, directoryUserMapper, ldapProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "ldap.active-directory.enabled", havingValue = "true")
    public ActiveDirectoryAdapter activeDirectoryAdapter(
            ActiveDirectoryLdapAuthenticationProvider adProvider,
            LdapContextSource ldapContextSource,
            DirectoryUserMapper directoryUserMapper,
            ActiveDirectoryProperties adProperties) {
        return new ActiveDirectoryAdapter(adProvider, ldapContextSource, directoryUserMapper, adProperties);
    }

    @Bean
    public JwtTokenProviderAdapter jwtTokenProviderAdapter(JwtProperties jwtProperties) {
        return new JwtTokenProviderAdapter(jwtProperties);
    }

    @Bean
    public TokenBlacklistRedisAdapter tokenBlacklistRedisAdapter(ReactiveStringRedisTemplate redisTemplate) {
        return new TokenBlacklistRedisAdapter(redisTemplate);
    }

    @Bean
    public RefreshTokenRedisAdapter refreshTokenRedisAdapter(
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        return new RefreshTokenRedisAdapter(redisTemplate, objectMapper);
    }

    @Bean
    public AuthAuditMongoAdapter authAuditMongoAdapter(AuthAuditRepository authAuditRepository) {
        return new AuthAuditMongoAdapter(authAuditRepository);
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

    @Bean
    public AuthenticationRestMapper authenticationRestMapper() {
        return new AuthenticationRestMapper();
    }

    @Bean
    public AuthenticationHandler authenticationHandler(
            SignInUseCase signInUseCase,
            SignOutUseCase signOutUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            AuthenticationRestMapper authenticationRestMapper,
            Validator validator) {
        return new AuthenticationHandler(signInUseCase, signOutUseCase, refreshTokenUseCase,
                authenticationRestMapper, validator);
    }
}
