package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.shared.infrastructure.properties.KeycloakProperties;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting Keycloak token claims and userinfo to AuthenticatedUser.
 */
public class KeycloakUserMapper {

    private final KeycloakProperties keycloakProperties;

    public KeycloakUserMapper(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    public AuthenticatedUser map(Map<String, Object> tokenClaims, Map<String, Object> userInfoClaims) {
        String userId = getClaimWithFallback(userInfoClaims, tokenClaims, "sub");
        String username = getClaimWithFallback(userInfoClaims, tokenClaims, "preferred_username");
        String email = getClaimWithFallback(userInfoClaims, tokenClaims, "email");
        String firstName = getStringClaim(userInfoClaims, "given_name");
        String lastName = getStringClaim(userInfoClaims, "family_name");

        Set<String> roles = extractRoles(tokenClaims);
        Set<String> groups = extractGroups(userInfoClaims);

        String effectiveEmail = Objects.requireNonNullElse(email, username + "@unknown.local");

        return AuthenticatedUser.builder()
                .userId(Objects.requireNonNullElse(userId, username))
                .username(username)
                .email(effectiveEmail)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .groups(groups)
                .enabled(true)
                .build();
    }

    @SuppressWarnings("unchecked")
    Set<String> extractRoles(Map<String, Object> tokenClaims) {
        Set<String> roles = new HashSet<>();
        KeycloakProperties.RoleMapping mapping = keycloakProperties.getRoleMapping();

        // Extract realm roles
        if (mapping.isUseRealmRoles()) {
            Optional.ofNullable((Map<String, Object>) tokenClaims.get("realm_access"))
                    .map(realmAccess -> (List<String>) realmAccess.get("roles"))
                    .ifPresent(roles::addAll);
        }

        // Extract client roles
        if (mapping.isUseClientRoles()) {
            String clientIdForRoles = Optional.ofNullable(mapping.getClientIdForRoles())
                    .filter(id -> !id.isBlank())
                    .orElse(keycloakProperties.getClientId());

            Optional.ofNullable((Map<String, Object>) tokenClaims.get("resource_access"))
                    .map(resourceAccess -> (Map<String, Object>) resourceAccess.get(clientIdForRoles))
                    .map(clientAccess -> (List<String>) clientAccess.get("roles"))
                    .ifPresent(roles::addAll);
        }

        // Filter and normalize roles: keep APP_ and ROLE_ prefixed roles
        return roles.stream()
                .filter(role -> role.startsWith("APP_") || role.startsWith("ROLE_"))
                .map(role -> role.startsWith("APP_") ? "ROLE_" + role.substring(4) : role)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    Set<String> extractGroups(Map<String, Object> claims) {
        if (claims.get("groups") instanceof List<?> groupsList) {
            return new HashSet<>((List<String>) groupsList);
        }
        return Collections.emptySet();
    }

    private String getStringClaim(Map<String, Object> claims, String key) {
        return Optional.ofNullable(claims.get(key))
                .map(Object::toString)
                .orElse(null);
    }

    private String getClaimWithFallback(Map<String, Object> primary, Map<String, Object> fallback, String key) {
        return Optional.ofNullable(getStringClaim(primary, key))
                .orElseGet(() -> getStringClaim(fallback, key));
    }
}
