package com.company.security.shared.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ldap.active-directory")
public class ActiveDirectoryProperties {

    private boolean enabled = false;
    private String domain;
}
