package com.company.security.password.domain.port.output;

import reactor.core.publisher.Mono;

/**
 * Output port for looking up user information from external service.
 */
public interface UserLookupPort {

    /**
     * Finds a user by email address via the Client Service.
     *
     * @param email the user's email
     * @return a Mono containing the user ID, or empty if not found
     */
    Mono<UserInfo> findByEmail(String email);

    record UserInfo(String userId, String email, String username) {}
}
