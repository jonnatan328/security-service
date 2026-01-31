package com.company.security.authentication.infrastructure.config;

import com.company.security.authentication.domain.port.input.RefreshTokenUseCase;
import com.company.security.authentication.domain.port.input.SignInUseCase;
import com.company.security.authentication.domain.port.input.SignOutUseCase;
import com.company.security.authentication.infrastructure.adapter.input.rest.handler.AuthenticationHandler;
import com.company.security.authentication.infrastructure.adapter.input.rest.mapper.AuthenticationRestMapper;
import com.company.security.authentication.infrastructure.adapter.output.directory.DirectoryUserMapper;
import com.company.security.authentication.infrastructure.adapter.output.directory.KeycloakDirectoryAdapter;
import com.company.security.authentication.infrastructure.adapter.output.directory.KeycloakUserMapper;
import com.company.security.authentication.infrastructure.adapter.output.directory.LdapDirectoryAdapter;
import com.company.security.authentication.infrastructure.adapter.output.persistence.AuthAuditMongoAdapter;
import com.company.security.authentication.infrastructure.adapter.output.persistence.repository.AuthAuditRepository;
import com.company.security.authentication.infrastructure.adapter.output.token.JwtTokenProviderAdapter;
import com.company.security.authentication.infrastructure.adapter.output.token.RefreshTokenRedisAdapter;
import com.company.security.authentication.infrastructure.adapter.output.token.TokenBlacklistRedisAdapter;
import com.company.security.shared.infrastructure.properties.JwtProperties;
import com.company.security.shared.infrastructure.properties.KeycloakProperties;
import com.company.security.shared.infrastructure.properties.LdapProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Infrastructure bean configuration for authentication feature.
 * Wires adapters, mappers and handlers.
 */
@Configuration
public class AuthenticationInfrastructureConfig {

    @Bean
    public DirectoryUserMapper directoryUserMapper() {
        return new DirectoryUserMapper();
    }

    @Bean
    @ConditionalOnProperty(name = "auth.provider", havingValue = "ldap", matchIfMissing = true)
    public LdapDirectoryAdapter ldapDirectoryAdapter(
            LdapTemplate ldapTemplate,
            LdapContextSource ldapContextSource,
            DirectoryUserMapper directoryUserMapper,
            LdapProperties ldapProperties) {
        return new LdapDirectoryAdapter(ldapTemplate, ldapContextSource, directoryUserMapper, ldapProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "auth.provider", havingValue = "keycloak")
    public KeycloakUserMapper keycloakUserMapper(KeycloakProperties keycloakProperties) {
        return new KeycloakUserMapper(keycloakProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "auth.provider", havingValue = "keycloak")
    public KeycloakDirectoryAdapter keycloakDirectoryAdapter(
            WebClient.Builder webClientBuilder,
            KeycloakProperties keycloakProperties,
            KeycloakUserMapper keycloakUserMapper,
            ObjectMapper objectMapper) {
        return new KeycloakDirectoryAdapter(webClientBuilder, keycloakProperties, keycloakUserMapper, objectMapper);
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
