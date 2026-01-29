package com.company.security.shared.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "security.password")
public class PasswordPolicyProperties {

    private long resetTokenExpiration = 30;
    private int minLength = 8;
    private int maxLength = 128;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSpecialChar = true;
    private int maxFailedAttempts = 5;
    private long lockDuration = 30;
    private String resetBaseUrl = "http://localhost:3000/reset-password";
}
