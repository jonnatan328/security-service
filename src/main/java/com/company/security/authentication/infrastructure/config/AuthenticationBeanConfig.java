package com.company.security.authentication.infrastructure.config;

import com.company.security.authentication.domain.service.AuthenticationDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean configuration for authentication feature.
 */
@Configuration
public class AuthenticationBeanConfig {

    @Bean
    public AuthenticationDomainService authenticationDomainService() {
        return new AuthenticationDomainService();
    }
}
