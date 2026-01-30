package com.company.security.password.infrastructure.config;

import com.company.security.password.domain.model.PasswordPolicy;
import com.company.security.password.domain.model.PasswordRecoverySettings;
import com.company.security.password.domain.port.input.RecoverPasswordUseCase;
import com.company.security.password.domain.port.input.ResetPasswordUseCase;
import com.company.security.password.domain.port.input.UpdatePasswordUseCase;
import com.company.security.password.domain.port.output.*;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.password.domain.usecase.RecoverPasswordUseCaseImpl;
import com.company.security.password.domain.usecase.ResetPasswordUseCaseImpl;
import com.company.security.password.domain.usecase.UpdatePasswordUseCaseImpl;
import com.company.security.password.infrastructure.adapter.input.rest.handler.PasswordHandler;
import com.company.security.password.infrastructure.adapter.input.rest.mapper.PasswordRestMapper;
import com.company.security.password.infrastructure.adapter.output.client.ClientServiceAdapter;
import com.company.security.password.infrastructure.adapter.output.directory.DirectoryPasswordAdapter;
import com.company.security.password.infrastructure.adapter.output.messaging.PasswordEventPublisherAdapter;
import com.company.security.password.infrastructure.adapter.output.persistence.PasswordAuditMongoAdapter;
import com.company.security.password.infrastructure.adapter.output.persistence.PasswordResetTokenMongoAdapter;
import com.company.security.password.infrastructure.adapter.output.persistence.repository.PasswordAuditRepository;
import com.company.security.password.infrastructure.adapter.output.persistence.repository.PasswordResetTokenRepository;
import com.company.security.shared.infrastructure.properties.LdapProperties;
import com.company.security.shared.infrastructure.properties.PasswordPolicyProperties;
import com.company.security.shared.infrastructure.properties.ServicesProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Bean configuration for password feature.
 */
@Configuration
public class PasswordBeanConfig {

    @Bean
    public PasswordPolicyService passwordPolicyService(PasswordPolicyProperties properties) {
        PasswordPolicy policy = PasswordPolicy.builder()
                .minLength(properties.getMinLength())
                .maxLength(properties.getMaxLength())
                .requireUppercase(properties.isRequireUppercase())
                .requireLowercase(properties.isRequireLowercase())
                .requireDigit(properties.isRequireDigit())
                .requireSpecialChar(properties.isRequireSpecialChar())
                .build();
        return new PasswordPolicyService(policy);
    }

    @Bean
    public PasswordRecoverySettings passwordRecoverySettings(PasswordPolicyProperties properties) {
        return new PasswordRecoverySettings(
                properties.getResetTokenExpiration(),
                properties.getResetBaseUrl());
    }

    @Bean
    public DirectoryPasswordAdapter directoryPasswordAdapter(
            LdapTemplate ldapTemplate,
            LdapContextSource ldapContextSource,
            LdapProperties ldapProperties) {
        return new DirectoryPasswordAdapter(ldapTemplate, ldapContextSource, ldapProperties);
    }

    @Bean
    public PasswordResetTokenMongoAdapter passwordResetTokenMongoAdapter(PasswordResetTokenRepository repository) {
        return new PasswordResetTokenMongoAdapter(repository);
    }

    @Bean
    public PasswordAuditMongoAdapter passwordAuditMongoAdapter(PasswordAuditRepository repository) {
        return new PasswordAuditMongoAdapter(repository);
    }

    @Bean
    public PasswordEventPublisherAdapter passwordEventPublisherAdapter(
            ReactiveKafkaProducerTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        return new PasswordEventPublisherAdapter(kafkaTemplate, objectMapper);
    }

    @Bean
    public ClientServiceAdapter clientServiceAdapter(
            WebClient.Builder webClientBuilder,
            ServicesProperties servicesProperties) {
        return new ClientServiceAdapter(webClientBuilder, servicesProperties);
    }

    @Bean
    public RecoverPasswordUseCase recoverPasswordUseCase(
            UserLookupPort userLookupPort,
            PasswordResetTokenPort passwordResetTokenPort,
            EventPublisherPort eventPublisherPort,
            PasswordAuditPort passwordAuditPort,
            PasswordRecoverySettings passwordRecoverySettings) {
        return new RecoverPasswordUseCaseImpl(userLookupPort, passwordResetTokenPort,
                eventPublisherPort, passwordAuditPort, passwordRecoverySettings);
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(
            PasswordResetTokenPort passwordResetTokenPort,
            DirectoryPasswordPort directoryPasswordPort,
            PasswordAuditPort passwordAuditPort,
            PasswordPolicyService passwordPolicyService) {
        return new ResetPasswordUseCaseImpl(passwordResetTokenPort, directoryPasswordPort,
                passwordAuditPort, passwordPolicyService);
    }

    @Bean
    public UpdatePasswordUseCase updatePasswordUseCase(
            DirectoryPasswordPort directoryPasswordPort,
            PasswordAuditPort passwordAuditPort,
            PasswordPolicyService passwordPolicyService) {
        return new UpdatePasswordUseCaseImpl(directoryPasswordPort, passwordAuditPort, passwordPolicyService);
    }

    @Bean
    public PasswordRestMapper passwordRestMapper() {
        return new PasswordRestMapper() {};
    }

    @Bean
    public PasswordHandler passwordHandler(
            RecoverPasswordUseCase recoverPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            UpdatePasswordUseCase updatePasswordUseCase,
            PasswordRestMapper passwordRestMapper,
            Validator validator) {
        return new PasswordHandler(recoverPasswordUseCase, resetPasswordUseCase,
                updatePasswordUseCase, passwordRestMapper, validator);
    }
}
