package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.exception.AccountDisabledException;
import com.company.security.authentication.domain.exception.AccountLockedException;
import com.company.security.authentication.domain.exception.DirectoryServiceException;
import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.port.output.DirectoryServicePort;
import com.company.security.shared.infrastructure.properties.ActiveDirectoryProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Active Directory adapter implementation.
 * Provides authentication specifically against Microsoft Active Directory.
 */
public class ActiveDirectoryAdapter implements DirectoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(ActiveDirectoryAdapter.class);

    private final ActiveDirectoryLdapAuthenticationProvider adProvider;
    private final LdapContextSource ldapContextSource;
    private final DirectoryUserMapper userMapper;
    private final ActiveDirectoryProperties adProperties;

    public ActiveDirectoryAdapter(
            ActiveDirectoryLdapAuthenticationProvider adProvider,
            LdapContextSource ldapContextSource,
            DirectoryUserMapper userMapper,
            ActiveDirectoryProperties adProperties) {
        this.adProvider = adProvider;
        this.ldapContextSource = ldapContextSource;
        this.userMapper = userMapper;
        this.adProperties = adProperties;
    }

    @Override
    @CircuitBreaker(name = "directoryService", fallbackMethod = "authenticateFallback")
    @Retry(name = "directoryService")
    @TimeLimiter(name = "directoryService")
    public Mono<AuthenticatedUser> authenticate(Credentials credentials) {
        return Mono.fromCallable(() -> {
            log.debug("Authenticating user via Active Directory: {}", credentials.username());

            try {
                // Build the UPN (User Principal Name) for AD authentication
                String upn = buildUserPrincipalName(credentials.username());

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(upn, credentials.password());

                // Authenticate against AD
                Authentication authentication = adProvider.authenticate(authToken);

                if (!authentication.isAuthenticated()) {
                    throw new InvalidCredentialsException(credentials.username());
                }

                // Get the LDAP context for the authenticated user
                DirContextOperations ctx = (DirContextOperations) authentication.getPrincipal();

                return userMapper.mapFromActiveDirectoryContext(ctx);

            } catch (BadCredentialsException e) {
                log.warn("AD authentication failed for user: {} - Invalid credentials", credentials.username());
                throw new InvalidCredentialsException(credentials.username());
            } catch (LockedException e) {
                log.warn("AD authentication failed for user: {} - Account locked", credentials.username());
                throw new AccountLockedException(credentials.username());
            } catch (DisabledException e) {
                log.warn("AD authentication failed for user: {} - Account disabled", credentials.username());
                throw new AccountDisabledException(credentials.username());
            } catch (Exception e) {
                if (e instanceof InvalidCredentialsException ||
                    e instanceof AccountLockedException ||
                    e instanceof AccountDisabledException) {
                    throw e;
                }
                log.error("AD error during authentication for user: {}", credentials.username(), e);
                throw new DirectoryServiceException("Failed to authenticate with Active Directory", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @CircuitBreaker(name = "directoryService", fallbackMethod = "findByUsernameFallback")
    @Retry(name = "directoryService")
    public Mono<AuthenticatedUser> findByUsername(String username) {
        return Mono.<AuthenticatedUser>fromCallable(() -> {
            log.debug("Looking up user via Active Directory: {}", username);

            try {
                // For AD, we need to search using sAMAccountName or userPrincipalName
                String searchFilter = String.format(
                        "(|(sAMAccountName=%s)(userPrincipalName=%s@%s))",
                        username,
                        username,
                        adProperties.getDomain()
                );

                // This would require a proper search implementation
                // For now, we'll throw an appropriate error
                throw new DirectoryServiceException(
                        "User lookup not supported for AD - use authenticate method instead");

            } catch (DirectoryServiceException e) {
                throw e;
            } catch (Exception e) {
                log.error("AD error during user lookup: {}", username, e);
                throw new DirectoryServiceException("Failed to lookup user in Active Directory", e);
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
                log.warn("Active Directory service unavailable: {}", e.getMessage());
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String buildUserPrincipalName(String username) {
        if (username.contains("@")) {
            return username;
        }
        return username + "@" + adProperties.getDomain();
    }

    // Fallback methods for circuit breaker
    private Mono<AuthenticatedUser> authenticateFallback(Credentials credentials, Throwable t) {
        log.error("Circuit breaker open for AD authentication, failing request for user: {}",
                credentials.username(), t);
        return Mono.error(new DirectoryServiceException("Directory service temporarily unavailable", t));
    }

    private Mono<AuthenticatedUser> findByUsernameFallback(String username, Throwable t) {
        log.error("Circuit breaker open for AD lookup, failing request for user: {}", username, t);
        return Mono.error(new DirectoryServiceException("Directory service temporarily unavailable", t));
    }
}
