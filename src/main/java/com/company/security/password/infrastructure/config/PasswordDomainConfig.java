package com.company.security.password.infrastructure.config;

import com.company.security.password.domain.model.PasswordPolicy;
import com.company.security.password.domain.model.PasswordRecoverySettings;
import com.company.security.password.domain.port.input.RecoverPasswordUseCase;
import com.company.security.password.domain.port.input.ResetPasswordUseCase;
import com.company.security.password.domain.port.input.UpdatePasswordUseCase;
import com.company.security.password.domain.port.output.DirectoryPasswordPort;
import com.company.security.password.domain.port.output.EventPublisherPort;
import com.company.security.password.domain.port.output.PasswordAuditPort;
import com.company.security.password.domain.port.output.PasswordResetTokenPort;
import com.company.security.password.domain.port.output.UserLookupPort;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.password.domain.usecase.RecoverPasswordUseCaseImpl;
import com.company.security.password.domain.usecase.ResetPasswordUseCaseImpl;
import com.company.security.password.domain.usecase.UpdatePasswordUseCaseImpl;
import com.company.security.shared.infrastructure.properties.PasswordPolicyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain bean configuration for password feature.
 * Wires domain services and use cases.
 */
@Configuration
public class PasswordDomainConfig {

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
}
