package com.company.security.shared.infrastructure.config.ldap;

import com.company.security.shared.infrastructure.properties.ActiveDirectoryProperties;
import com.company.security.shared.infrastructure.properties.LdapProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

@Configuration
public class LdapConfig {

    private final LdapProperties ldapProperties;
    private final ActiveDirectoryProperties activeDirectoryProperties;

    public LdapConfig(LdapProperties ldapProperties,
                      ActiveDirectoryProperties activeDirectoryProperties) {
        this.ldapProperties = ldapProperties;
        this.activeDirectoryProperties = activeDirectoryProperties;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapProperties.getUrl());
        contextSource.setBase(ldapProperties.getBase());
        contextSource.setUserDn(ldapProperties.getManagerDn());
        contextSource.setPassword(ldapProperties.getManagerPassword());
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        return new LdapTemplate(ldapContextSource);
    }

    @Bean
    @ConditionalOnProperty(prefix = "ldap.active-directory", name = "enabled", havingValue = "true")
    public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider =
                new ActiveDirectoryLdapAuthenticationProvider(
                        activeDirectoryProperties.getDomain(),
                        ldapProperties.getUrl()
                );
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        return provider;
    }
}
