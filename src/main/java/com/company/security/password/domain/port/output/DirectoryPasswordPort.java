package com.company.security.password.domain.port.output;

import reactor.core.publisher.Mono;

/**
 * Output port for directory password operations.
 * Handles password changes in LDAP/Active Directory.
 */
public interface DirectoryPasswordPort {

    Mono<Boolean> verifyPassword(String userId, String currentPassword);

    Mono<Void> changePassword(String userId, String newPassword);

    Mono<Void> resetPassword(String userId, String newPassword);
}
