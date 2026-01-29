package com.company.security.token.domain.model;

/**
 * Enumeration representing the status of a token.
 * No external dependencies - pure domain enum.
 */
public enum TokenStatus {
    VALID,
    EXPIRED,
    REVOKED,
    INVALID,
    MALFORMED
}
