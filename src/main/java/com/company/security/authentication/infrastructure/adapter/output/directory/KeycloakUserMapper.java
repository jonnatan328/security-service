package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.shared.infrastructure.properties.KeycloakProperties;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper for converting Keycloak token claims and userinfo to AuthenticatedUser.
 */
public class KeycloakUserMapper {

    private final KeycloakProperties keycloakProperties;

    public KeycloakUserMapper(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    @SuppressWarnings("unchecked")
    public AuthenticatedUser map(Map<String, Object> tokenClaims, Map<String, Object> userInfoClaims) {
        String userId = getStringClaim(userInfoClaims, "sub");
        String username = getStringClaim(userInfoClaims, "preferred_username");
        String email = getStringClaim(userInfoClaims, "email");
        String firstName = getStringClaim(userInfoClaims, "given_name");
        String lastName = getStringClaim(userInfoClaims, "family_name");

        // Fallbacks from token claims if userinfo is missing values
        if (userId == null) {
            userId = getStringClaim(tokenClaims, "sub");
        }
        if (username == null) {
            username = getStringClaim(tokenClaims, "preferred_username");
        }
        if (email == null) {
            email = getStringClaim(tokenClaims, "email");
        }

        Set<String> roles = extractRoles(tokenClaims);
        Set<String> groups = extractGroups(userInfoClaims);

        String effectiveEmail = email != null ? email : username + "@unknown.local";

        return AuthenticatedUser.builder()
                .userId(userId != null ? userId : username)
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
            Map<String, Object> realmAccess = (Map<String, Object>) tokenClaims.get("realm_access");
            if (realmAccess != null) {
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                if (realmRoles != null) {
                    roles.addAll(realmRoles);
                }
            }
        }

        // Extract client roles
        if (mapping.isUseClientRoles()) {
            String clientIdForRoles = mapping.getClientIdForRoles();
            if (clientIdForRoles == null || clientIdForRoles.isBlank()) {
                clientIdForRoles = keycloakProperties.getClientId();
            }
            Map<String, Object> resourceAccess = (Map<String, Object>) tokenClaims.get("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientIdForRoles);
                if (clientAccess != null) {
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    if (clientRoles != null) {
                        roles.addAll(clientRoles);
                    }
                }
            }
        }

        // Filter and normalize roles: keep APP_ and ROLE_ prefixed roles
        return roles.stream()
                .filter(role -> role.startsWith("APP_") || role.startsWith("ROLE_"))
                .map(role -> {
                    if (role.startsWith("APP_")) {
                        return "ROLE_" + role.substring(4);
                    }
                    return role;
                })
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    Set<String> extractGroups(Map<String, Object> claims) {
        Object groupsClaim = claims.get("groups");
        if (groupsClaim instanceof List) {
            return new HashSet<>((List<String>) groupsClaim);
        }
        return Collections.emptySet();
    }

    private String getStringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value != null ? value.toString() : null;
    }
}
