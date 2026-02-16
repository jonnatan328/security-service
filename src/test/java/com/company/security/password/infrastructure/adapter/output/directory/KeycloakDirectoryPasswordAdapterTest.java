package com.company.security.password.infrastructure.adapter.output.directory;

import com.company.security.shared.infrastructure.properties.KeycloakProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakDirectoryPasswordAdapterTest {

    private MockWebServer mockWebServer;
    private KeycloakDirectoryPasswordAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl(mockWebServer.url("/").toString());
        properties.setRealm("test-realm");
        properties.setClientId("test-client");
        properties.setClientSecret("test-secret");

        adapter = new KeycloakDirectoryPasswordAdapter(WebClient.builder(), properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void verifyPassword_withValidCredentials_returnsTrue() {
        // Client credentials token for user lookup
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"admin-token\"}")
                .setHeader("Content-Type", "application/json"));
        // User lookup by ID
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"username\": \"john.doe\"}")
                .setHeader("Content-Type", "application/json"));
        // ROPC token (password verification)
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"user-token\"}")
                .setHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.verifyPassword("user-123", "correct-password"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();
    }

    @Test
    void verifyPassword_withInvalidCredentials_returnsFalse() {
        // Client credentials token for user lookup
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"admin-token\"}")
                .setHeader("Content-Type", "application/json"));
        // User lookup by ID
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"username\": \"john.doe\"}")
                .setHeader("Content-Type", "application/json"));
        // ROPC token fails
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\": \"invalid_grant\"}")
                .setHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.verifyPassword("user-123", "wrong-password"))
                .assertNext(result -> assertThat(result).isFalse())
                .verifyComplete();
    }

    @Test
    void resetPassword_withValidInput_completes() {
        // Client credentials token
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"admin-token\"}")
                .setHeader("Content-Type", "application/json"));
        // Reset password
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        StepVerifier.create(adapter.resetPassword("user-123", "newPassword!"))
                .verifyComplete();
    }

    @Test
    void resetPassword_withKeycloakError_throwsException() {
        // Client credentials token
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"admin-token\"}")
                .setHeader("Content-Type", "application/json"));
        // Reset password fails
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"error\": \"forbidden\"}")
                .setHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.resetPassword("user-123", "newPassword!"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void changePassword_delegatesToResetPassword() {
        // Client credentials token
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"access_token\": \"admin-token\"}")
                .setHeader("Content-Type", "application/json"));
        // Reset password
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        StepVerifier.create(adapter.changePassword("user-123", "newPassword!"))
                .verifyComplete();
    }

    @Test
    void resetPassword_withNoAccessToken_throwsException() {
        // Client credentials token without access_token field
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"token_type\": \"bearer\"}")
                .setHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.resetPassword("user-123", "newPassword!"))
                .expectError(IllegalStateException.class)
                .verify();
    }
}
