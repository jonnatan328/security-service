package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.shared.infrastructure.properties.KeycloakProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakUserMapperTest {

    private KeycloakProperties keycloakProperties;
    private KeycloakUserMapper mapper;

    @BeforeEach
    void setUp() {
        keycloakProperties = new KeycloakProperties();
        keycloakProperties.setClientId("security-service");
        keycloakProperties.getRoleMapping().setUseRealmRoles(true);
        keycloakProperties.getRoleMapping().setUseClientRoles(true);
        mapper = new KeycloakUserMapper(keycloakProperties);
    }

    @Test
    void shouldMapAllClaimsToAuthenticatedUser() {
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("sub", "user-123");
        tokenClaims.put("realm_access", Map.of("roles", List.of("APP_ADMIN", "APP_USER")));

        Map<String, Object> userInfoClaims = new HashMap<>();
        userInfoClaims.put("sub", "user-123");
        userInfoClaims.put("preferred_username", "jdoe");
        userInfoClaims.put("email", "jdoe@example.com");
        userInfoClaims.put("given_name", "John");
        userInfoClaims.put("family_name", "Doe");
        userInfoClaims.put("groups", List.of("engineering", "devops"));

        AuthenticatedUser user = mapper.map(tokenClaims, userInfoClaims);

        assertThat(user.userId()).isEqualTo("user-123");
        assertThat(user.username()).isEqualTo("jdoe");
        assertThat(user.email().value()).isEqualTo("jdoe@example.com");
        assertThat(user.firstName()).isEqualTo("John");
        assertThat(user.lastName()).isEqualTo("Doe");
        assertThat(user.roles()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        assertThat(user.groups()).containsExactlyInAnyOrder("engineering", "devops");
        assertThat(user.enabled()).isTrue();
    }

    @Test
    void shouldExtractRealmRolesWithAppPrefix() {
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("realm_access", Map.of("roles", List.of("APP_ADMIN", "uma_authorization", "offline_access")));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).containsExactly("ROLE_ADMIN");
    }

    @Test
    void shouldExtractRealmRolesWithRolePrefix() {
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("realm_access", Map.of("roles", List.of("ROLE_MANAGER", "default-roles")));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).containsExactly("ROLE_MANAGER");
    }

    @Test
    void shouldExtractClientRoles() {
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("resource_access", Map.of(
                "security-service", Map.of("roles", List.of("APP_VIEWER", "APP_EDITOR"))));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).containsExactlyInAnyOrder("ROLE_VIEWER", "ROLE_EDITOR");
    }

    @Test
    void shouldUseCustomClientIdForRoles() {
        keycloakProperties.getRoleMapping().setClientIdForRoles("custom-client");
        mapper = new KeycloakUserMapper(keycloakProperties);

        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("resource_access", Map.of(
                "custom-client", Map.of("roles", List.of("APP_CUSTOM"))));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).containsExactly("ROLE_CUSTOM");
    }

    @Test
    void shouldCombineRealmAndClientRoles() {
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("realm_access", Map.of("roles", List.of("APP_ADMIN")));
        tokenClaims.put("resource_access", Map.of(
                "security-service", Map.of("roles", List.of("APP_VIEWER"))));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_VIEWER");
    }

    @Test
    void shouldReturnEmptyRolesWhenNoRealmAccess() {
        Map<String, Object> tokenClaims = new HashMap<>();

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).isEmpty();
    }

    @Test
    void shouldFilterOutNonPrefixedRoles() {
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("realm_access", Map.of("roles",
                List.of("uma_authorization", "offline_access", "default-roles-realm", "APP_USER")));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).containsExactly("ROLE_USER");
    }

    @Test
    void shouldExtractGroups() {
        Map<String, Object> claims = Map.of("groups", List.of("group1", "group2"));

        Set<String> groups = mapper.extractGroups(claims);

        assertThat(groups).containsExactlyInAnyOrder("group1", "group2");
    }

    @Test
    void shouldReturnEmptyGroupsWhenMissing() {
        Map<String, Object> claims = new HashMap<>();

        Set<String> groups = mapper.extractGroups(claims);

        assertThat(groups).isEmpty();
    }

    @Test
    void shouldFallbackToTokenClaimsWhenUserInfoMissesValues() {
        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("sub", "token-sub-123");
        tokenClaims.put("preferred_username", "token-user");
        tokenClaims.put("email", "token@example.com");

        Map<String, Object> userInfoClaims = new HashMap<>();
        // userinfo has no sub, preferred_username, or email

        AuthenticatedUser user = mapper.map(tokenClaims, userInfoClaims);

        assertThat(user.userId()).isEqualTo("token-sub-123");
        assertThat(user.username()).isEqualTo("token-user");
        assertThat(user.email().value()).isEqualTo("token@example.com");
    }

    @Test
    void shouldUseFallbackEmailWhenMissing() {
        Map<String, Object> tokenClaims = new HashMap<>();

        Map<String, Object> userInfoClaims = new HashMap<>();
        userInfoClaims.put("sub", "user-456");
        userInfoClaims.put("preferred_username", "jsmith");

        AuthenticatedUser user = mapper.map(tokenClaims, userInfoClaims);

        assertThat(user.email().value()).isEqualTo("jsmith@unknown.local");
    }

    @Test
    void shouldNotExtractRealmRolesWhenDisabled() {
        keycloakProperties.getRoleMapping().setUseRealmRoles(false);
        mapper = new KeycloakUserMapper(keycloakProperties);

        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("realm_access", Map.of("roles", List.of("APP_ADMIN")));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).isEmpty();
    }

    @Test
    void shouldNotExtractClientRolesWhenDisabled() {
        keycloakProperties.getRoleMapping().setUseClientRoles(false);
        mapper = new KeycloakUserMapper(keycloakProperties);

        Map<String, Object> tokenClaims = new HashMap<>();
        tokenClaims.put("resource_access", Map.of(
                "security-service", Map.of("roles", List.of("APP_VIEWER"))));

        Set<String> roles = mapper.extractRoles(tokenClaims);

        assertThat(roles).isEmpty();
    }
}
