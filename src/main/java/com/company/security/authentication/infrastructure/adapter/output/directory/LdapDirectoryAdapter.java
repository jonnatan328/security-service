package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.exception.DirectoryServiceException;
import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.infrastructure.application.port.output.DirectoryServicePort;
import com.company.security.shared.infrastructure.properties.LdapProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * LDAP Directory Service adapter implementation.
 * Provides authentication against generic LDAP servers.
 */
@Component
@ConditionalOnProperty(name = "ldap.active-directory.enabled", havingValue = "false", matchIfMissing = true)
public class LdapDirectoryAdapter implements DirectoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(LdapDirectoryAdapter.class);

    private final LdapTemplate ldapTemplate;
    private final LdapContextSource ldapContextSource;
    private final DirectoryUserMapper userMapper;
    private final LdapProperties ldapProperties;

    public LdapDirectoryAdapter(
            LdapTemplate ldapTemplate,
            LdapContextSource ldapContextSource,
            DirectoryUserMapper userMapper,
            LdapProperties ldapProperties) {
        this.ldapTemplate = ldapTemplate;
        this.ldapContextSource = ldapContextSource;
        this.userMapper = userMapper;
        this.ldapProperties = ldapProperties;
    }

    @Override
    @CircuitBreaker(name = "directoryService", fallbackMethod = "authenticateFallback")
    @Retry(name = "directoryService")
    @TimeLimiter(name = "directoryService")
    public Mono<AuthenticatedUser> authenticate(Credentials credentials) {
        return Mono.fromCallable(() -> {
            log.debug("Authenticating user via LDAP: {}", credentials.username());

            try {
                // Build the user DN
                String userDn = buildUserDn(credentials.username());

                // Attempt to authenticate
                boolean authenticated = ldapTemplate.authenticate(
                        ldapProperties.getUserSearchBase(),
                        buildSearchFilter(credentials.username()),
                        credentials.password()
                );

                if (!authenticated) {
                    log.warn("LDAP authentication failed for user: {}", credentials.username());
                    throw new InvalidCredentialsException(credentials.username());
                }

                // Lookup user details
                DirContextOperations ctx = ldapTemplate.searchForContext(
                        LdapQueryBuilder.query()
                                .base(ldapProperties.getUserSearchBase())
                                .filter(buildSearchFilter(credentials.username()))
                );

                return userMapper.mapFromLdapContext(ctx, credentials.username());

            } catch (AuthenticationException e) {
                log.warn("LDAP authentication failed for user: {} - {}", credentials.username(), e.getMessage());
                throw new InvalidCredentialsException(credentials.username());
            } catch (Exception e) {
                log.error("LDAP error during authentication for user: {}", credentials.username(), e);
                throw new DirectoryServiceException("Failed to authenticate", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @CircuitBreaker(name = "directoryService", fallbackMethod = "findByUsernameFallback")
    @Retry(name = "directoryService")
    public Mono<AuthenticatedUser> findByUsername(String username) {
        return Mono.fromCallable(() -> {
            log.debug("Looking up user via LDAP: {}", username);

            try {
                LdapQuery query = LdapQueryBuilder.query()
                        .base(ldapProperties.getUserSearchBase())
                        .filter(buildSearchFilter(username));

                DirContextOperations ctx = ldapTemplate.searchForContext(query);
                return userMapper.mapFromLdapContext(ctx, username);

            } catch (Exception e) {
                log.error("LDAP error during user lookup: {}", username, e);
                throw new DirectoryServiceException("Failed to lookup user", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> isAvailable() {
        return Mono.fromCallable(() -> {
            try {
                ldapContextSource.getReadOnlyContext();
                return true;
            } catch (Exception e) {
                log.warn("LDAP service unavailable: {}", e.getMessage());
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String buildUserDn(String username) {
        return String.format("%s=%s,%s",
                ldapProperties.getUserDnAttribute(),
                username,
                ldapProperties.getUserSearchBase());
    }

    private String buildSearchFilter(String username) {
        return new EqualsFilter(ldapProperties.getUserDnAttribute(), username).encode();
    }

    // Fallback methods for circuit breaker
    private Mono<AuthenticatedUser> authenticateFallback(Credentials credentials, Throwable t) {
        log.error("Circuit breaker open for LDAP authentication, failing request for user: {}",
                credentials.username(), t);
        return Mono.error(new DirectoryServiceException("Directory service temporarily unavailable", t));
    }

    private Mono<AuthenticatedUser> findByUsernameFallback(String username, Throwable t) {
        log.error("Circuit breaker open for LDAP lookup, failing request for user: {}", username, t);
        return Mono.error(new DirectoryServiceException("Directory service temporarily unavailable", t));
    }
}
