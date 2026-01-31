package com.company.security.password.infrastructure.config;

import com.company.security.password.domain.port.input.RecoverPasswordUseCase;
import com.company.security.password.domain.port.input.ResetPasswordUseCase;
import com.company.security.password.domain.port.input.UpdatePasswordUseCase;
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
import com.company.security.shared.infrastructure.properties.ServicesProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Infrastructure bean configuration for password feature.
 * Wires adapters, mappers and handlers.
 */
@Configuration
public class PasswordInfrastructureConfig {

    @Bean
    @ConditionalOnExpression("'${auth.provider:ldap}' != 'keycloak'")
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
