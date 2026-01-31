package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.exception.AccountDisabledException;
import com.company.security.authentication.domain.exception.DirectoryServiceException;
import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.shared.infrastructure.properties.KeycloakProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakDirectoryAdapterTest {

    private MockWebServer mockWebServer;
    private KeycloakDirectoryAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString();
        // Remove trailing slash
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl(baseUrl);
        properties.setRealm("test-realm");
        properties.setClientId("test-client");
        properties.setClientSecret("test-secret");
        properties.getRoleMapping().setUseRealmRoles(true);
        properties.getRoleMapping().setUseClientRoles(true);

        objectMapper = new ObjectMapper();
        KeycloakUserMapper userMapper = new KeycloakUserMapper(properties);

        adapter = new KeycloakDirectoryAdapter(
                WebClient.builder().baseUrl(baseUrl),
                properties,
                userMapper,
                objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void authenticateShouldReturnUserOnSuccess() throws Exception {
        // Build a fake JWT access_token with claims in the payload
        Map<String, Object> tokenPayload = Map.of(
                "sub", "user-123",
                "preferred_username", "jdoe",
                "email", "jdoe@example.com",
                "realm_access", Map.of("roles", java.util.List.of("APP_ADMIN")));
        String payloadJson = objectMapper.writeValueAsString(tokenPayload);
        String fakeJwt = "header." + Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes()) + ".sig";

        // Token endpoint response
        Map<String, Object> tokenResponse = Map.of(
                "access_token", fakeJwt,
                "token_type", "Bearer",
                "expires_in", 300);
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(tokenResponse))
                .addHeader("Content-Type", "application/json"));

        // Userinfo endpoint response
        Map<String, Object> userInfoResponse = Map.of(
                "sub", "user-123",
                "preferred_username", "jdoe",
                "email", "jdoe@example.com",
                "given_name", "John",
                "family_name", "Doe");
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(userInfoResponse))
                .addHeader("Content-Type", "application/json"));

        Credentials credentials = Credentials.of("jdoe", "password123");

        StepVerifier.create(adapter.authenticate(credentials))
                .assertNext(user -> {
                    assertThat(user.userId()).isEqualTo("user-123");
                    assertThat(user.username()).isEqualTo("jdoe");
                    assertThat(user.email().value()).isEqualTo("jdoe@example.com");
                    assertThat(user.firstName()).isEqualTo("John");
                    assertThat(user.lastName()).isEqualTo("Doe");
                    assertThat(user.roles()).contains("ROLE_ADMIN");
                    assertThat(user.enabled()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void authenticateShouldThrowInvalidCredentialsOnInvalidGrant() throws Exception {
        Map<String, Object> errorResponse = Map.of(
                "error", "invalid_grant",
                "error_description", "Invalid user credentials");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody(objectMapper.writeValueAsString(errorResponse))
                .addHeader("Content-Type", "application/json"));

        Credentials credentials = Credentials.of("jdoe", "wrong-password");

        StepVerifier.create(adapter.authenticate(credentials))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void authenticateShouldThrowAccountDisabledOnDisabledAccount() throws Exception {
        Map<String, Object> errorResponse = Map.of(
                "error", "invalid_grant",
                "error_description", "Account disabled");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(objectMapper.writeValueAsString(errorResponse))
                .addHeader("Content-Type", "application/json"));

        Credentials credentials = Credentials.of("disabled-user", "password");

        StepVerifier.create(adapter.authenticate(credentials))
                .expectError(AccountDisabledException.class)
                .verify();
    }

    @Test
    void authenticateShouldThrowDirectoryServiceExceptionOnUnknownError() throws Exception {
        Map<String, Object> errorResponse = Map.of(
                "error", "server_error",
                "error_description", "Internal server error");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody(objectMapper.writeValueAsString(errorResponse))
                .addHeader("Content-Type", "application/json"));

        Credentials credentials = Credentials.of("jdoe", "password");

        StepVerifier.create(adapter.authenticate(credentials))
                .expectError(DirectoryServiceException.class)
                .verify();
    }

    @Test
    void isAvailableShouldReturnTrueWhenRealmEndpointResponds() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"realm\":\"test-realm\"}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.isAvailable())
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isAvailableShouldReturnFalseOnError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(503));

        StepVerifier.create(adapter.isAvailable())
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void findByUsernameShouldReturnUserFromAdminApi() throws Exception {
        // Client credentials token response
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "admin-token",
                "token_type", "Bearer",
                "expires_in", 300);
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(tokenResponse))
                .addHeader("Content-Type", "application/json"));

        // Admin API user search response
        java.util.List<Map<String, Object>> usersResponse = java.util.List.of(Map.of(
                "id", "user-789",
                "username", "jsmith",
                "email", "jsmith@example.com",
                "firstName", "Jane",
                "lastName", "Smith",
                "enabled", true));
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(usersResponse))
                .addHeader("Content-Type", "application/json"));

        // Realm role mappings response
        java.util.List<Map<String, Object>> rolesResponse = java.util.List.of(
                Map.of("name", "APP_USER"),
                Map.of("name", "default-roles-test-realm"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(rolesResponse))
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.findByUsername("jsmith"))
                .assertNext(user -> {
                    assertThat(user.userId()).isEqualTo("user-789");
                    assertThat(user.username()).isEqualTo("jsmith");
                    assertThat(user.email().value()).isEqualTo("jsmith@example.com");
                    assertThat(user.firstName()).isEqualTo("Jane");
                    assertThat(user.lastName()).isEqualTo("Smith");
                    assertThat(user.roles()).containsExactly("ROLE_USER");
                    assertThat(user.enabled()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void findByUsernameShouldThrowWhenUserNotFound() throws Exception {
        // Client credentials token response
        Map<String, Object> tokenResponse = Map.of(
                "access_token", "admin-token",
                "token_type", "Bearer",
                "expires_in", 300);
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(tokenResponse))
                .addHeader("Content-Type", "application/json"));

        // Admin API returns empty array
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.findByUsername("nonexistent"))
                .expectError(DirectoryServiceException.class)
                .verify();
    }

    @Test
    void decodeTokenPayloadShouldReturnClaimsFromValidJwt() throws Exception {
        Map<String, Object> payload = Map.of("sub", "user-1", "name", "Test");
        String payloadJson = objectMapper.writeValueAsString(payload);
        String fakeJwt = "header." + Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes()) + ".sig";

        Map<String, Object> result = adapter.decodeTokenPayload(fakeJwt);

        assertThat(result).containsEntry("sub", "user-1");
        assertThat(result).containsEntry("name", "Test");
    }

    @Test
    void decodeTokenPayloadShouldReturnEmptyMapForInvalidToken() {
        Map<String, Object> result = adapter.decodeTokenPayload("invalid-token");

        assertThat(result).isEmpty();
    }
}
