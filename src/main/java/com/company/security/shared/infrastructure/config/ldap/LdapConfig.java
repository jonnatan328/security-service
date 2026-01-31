package com.company.security.shared.infrastructure.config.ldap;

import com.company.security.shared.infrastructure.properties.LdapProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfig {

    private final LdapProperties ldapProperties;

    public LdapConfig(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    @Bean
    @ConditionalOnExpression("'${auth.provider:ldap}' != 'keycloak'")
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapProperties.getUrl());
        contextSource.setBase(ldapProperties.getBase());
        contextSource.setUserDn(ldapProperties.getManagerDn());
        contextSource.setPassword(ldapProperties.getManagerPassword());
        return contextSource;
    }

    @Bean
    @ConditionalOnExpression("'${auth.provider:ldap}' != 'keycloak'")
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        return new LdapTemplate(ldapContextSource);
    }
}
