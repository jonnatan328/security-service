package com.company.security.shared.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ldap")
public class LdapProperties {

    private String url;
    private String base;
    private String managerDn;
    private String managerPassword;
    private String userSearchBase;
    private String userSearchFilter;
    private String userDnAttribute = "uid";
}
