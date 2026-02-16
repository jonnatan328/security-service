package com.company.security.password.infrastructure.adapter.output.directory;

import com.company.security.password.domain.port.output.DirectoryPasswordPort;
import com.company.security.shared.infrastructure.properties.LdapProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

public class DirectoryPasswordAdapter implements DirectoryPasswordPort {

    private static final Logger log = LoggerFactory.getLogger(DirectoryPasswordAdapter.class);

    private final LdapTemplate ldapTemplate;
    private final LdapContextSource ldapContextSource;
    private final LdapProperties ldapProperties;

    public DirectoryPasswordAdapter(LdapTemplate ldapTemplate,
                                     LdapContextSource ldapContextSource,
                                     LdapProperties ldapProperties) {
        this.ldapTemplate = ldapTemplate;
        this.ldapContextSource = ldapContextSource;
        this.ldapProperties = ldapProperties;
    }

    @Override
    @CircuitBreaker(name = "directoryService")
    @Retry(name = "directoryService")
    public Mono<Boolean> verifyPassword(String userId, String currentPassword) {
        return Mono.fromCallable(() -> {
            try {
                String userDn = buildUserDn(userId);
                ldapContextSource.getContext(userDn, currentPassword);
                return true;
            } catch (Exception e) {
                log.debug("Password verification failed for user: {}", userId);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @CircuitBreaker(name = "directoryService")
    @Retry(name = "directoryService")
    @SuppressWarnings("java:S2139") // Exception is logged and rethrown with context
    public Mono<Void> changePassword(String userId, String newPassword) {
        return Mono.fromRunnable(() -> {
            try {
                String userDn = buildUserDn(userId);
                ModificationItem[] mods = new ModificationItem[]{
                        new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                new BasicAttribute("userPassword", newPassword))
                };
                ldapTemplate.modifyAttributes(userDn, mods);
                log.info("Password changed in directory for user: {}", userId);
            } catch (Exception e) {
                log.error("Failed to change password in directory for user: {}", userId, e);
                throw new IllegalStateException("Failed to change password in directory", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    @CircuitBreaker(name = "directoryService")
    @Retry(name = "directoryService")
    public Mono<Void> resetPassword(String userId, String newPassword) {
        return changePassword(userId, newPassword);
    }

    private String buildUserDn(String userId) {
        return String.format("%s=%s,%s",
                ldapProperties.getUserDnAttribute(),
                userId,
                ldapProperties.getUserSearchBase());
    }
}
