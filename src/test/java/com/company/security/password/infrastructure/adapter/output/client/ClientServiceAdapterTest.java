package com.company.security.password.infrastructure.adapter.output.client;

import com.company.security.shared.infrastructure.properties.ServicesProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ClientServiceAdapterTest {

    private MockWebServer mockWebServer;
    private ClientServiceAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ServicesProperties properties = new ServicesProperties();
        ServicesProperties.ServiceConfig clientConfig = new ServicesProperties.ServiceConfig();
        clientConfig.setBaseUrl(mockWebServer.url("/").toString());
        properties.setClientService(clientConfig);

        adapter = new ClientServiceAdapter(WebClient.builder(), properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void findByEmail_withExistingUser_returnsUserInfo() {
        String responseBody = """
                {"userId": "user-123", "email": "john@company.com", "username": "john.doe", "firstName": "John", "lastName": "Doe"}
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json"));

        StepVerifier.create(adapter.findByEmail("john@company.com"))
                .assertNext(userInfo -> {
                    assertThat(userInfo.userId()).isEqualTo("user-123");
                    assertThat(userInfo.email()).isEqualTo("john@company.com");
                    assertThat(userInfo.username()).isEqualTo("john.doe");
                })
                .verifyComplete();
    }

    @Test
    void findByEmail_withServerError_propagatesError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        StepVerifier.create(adapter.findByEmail("john@company.com"))
                .expectError()
                .verify();
    }
}
