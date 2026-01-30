package com.company.security.authentication.domain.port.output;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.Credentials;
import reactor.core.publisher.Mono;

/**
 * Output port for directory service operations (LDAP/Active Directory).
 * Provides an abstraction over different directory service implementations.
 */
public interface DirectoryServicePort {

    /**
     * Authenticates a user against the directory service.
     *
     * @param credentials the user credentials
     * @return a Mono containing the authenticated user if successful
     */
    Mono<AuthenticatedUser> authenticate(Credentials credentials);

    /**
     * Looks up a user by username without authentication.
     *
     * @param username the username to look up
     * @return a Mono containing the user if found
     */
    Mono<AuthenticatedUser> findByUsername(String username);

    /**
     * Checks if the directory service is available.
     *
     * @return a Mono containing true if the service is available
     */
    Mono<Boolean> isAvailable();
}
