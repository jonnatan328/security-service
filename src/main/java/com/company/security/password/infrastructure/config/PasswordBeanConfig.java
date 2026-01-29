package com.company.security.password.infrastructure.config;

import com.company.security.password.domain.model.PasswordPolicy;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.shared.infrastructure.properties.PasswordPolicyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
