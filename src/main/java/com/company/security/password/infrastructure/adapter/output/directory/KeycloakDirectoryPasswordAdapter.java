package com.company.security.password.infrastructure.adapter.output.directory;

import com.company.security.password.domain.port.output.DirectoryPasswordPort;
import com.company.security.shared.infrastructure.properties.KeycloakProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class KeycloakDirectoryPasswordAdapter implements DirectoryPasswordPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakDirectoryPasswordAdapter.class);

    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;

    public KeycloakDirectoryPasswordAdapter(
            WebClient.Builder webClientBuilder,
            KeycloakProperties keycloakProperties) {
        this.webClient = webClientBuilder
                .baseUrl(keycloakProperties.getServerUrl())
                .build();
        this.keycloakProperties = keycloakProperties;
    }

    @Override
    @CircuitBreaker(name = "keycloakService")
    @Retry(name = "keycloakService")
    public Mono<Boolean> verifyPassword(String userId, String currentPassword) {
        return lookupUsernameById(userId)
                .flatMap(username -> requestToken(username, currentPassword)
                        .map(response -> true)
                        .onErrorReturn(false));
    }

    @Override
    @CircuitBreaker(name = "keycloakService")
    @Retry(name = "keycloakService")
    public Mono<Void> changePassword(String userId, String newPassword) {
        return resetPassword(userId, newPassword);
    }

    @Override
    @CircuitBreaker(name = "keycloakService")
    @Retry(name = "keycloakService")
    public Mono<Void> resetPassword(String userId, String newPassword) {
        log.debug("Resetting password via Keycloak Admin API for user: {}", userId);

        return requestClientCredentialsToken()
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    if (accessToken == null) {
                        return Mono.error(new IllegalStateException(
                                "No access_token in client credentials response"));
                    }
                    return setUserPassword(accessToken, userId, newPassword);
                });
    }

    private Mono<Void> setUserPassword(String accessToken, String userId, String newPassword) {
        String uri = String.format("/admin/realms/%s/users/%s/reset-password",
                keycloakProperties.getRealm(), userId);

        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false
        );

        return webClient.put()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credential)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Failed to reset password via Keycloak for user {}: {}", userId, body);
                                    return Mono.error(new IllegalStateException(
                                            "Failed to reset password in Keycloak: " + body));
                                }))
                .toBodilessEntity()
                .doOnSuccess(r -> log.info("Password reset via Keycloak for user: {}", userId))
                .then();
    }

    private Mono<String> lookupUsernameById(String userId) {
        return requestClientCredentialsToken()
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    if (accessToken == null) {
                        return Mono.error(new IllegalStateException(
                                "No access_token in client credentials response"));
                    }
                    String uri = String.format("/admin/realms/%s/users/%s",
                            keycloakProperties.getRealm(), userId);

                    return webClient.get()
                            .uri(uri)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response ->
                                    response.bodyToMono(String.class)
                                            .flatMap(body -> Mono.error(new IllegalStateException(
                                                    "Failed to lookup user in Keycloak: " + body))))
                            .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                            .map(userData -> (String) userData.get("username"));
                });
    }

    private Mono<Map<String, Object>> requestToken(String username, String password) {
        String tokenUri = String.format("/realms/%s/protocol/openid-connect/token",
                keycloakProperties.getRealm());

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("client_secret", keycloakProperties.getClientSecret())
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "Password verification failed: " + body))))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<>() {});
    }

    @SuppressWarnings("unchecked")
    private Mono<Map<String, Object>> requestClientCredentialsToken() {
        String tokenUri = String.format("/realms/%s/protocol/openid-connect/token",
                keycloakProperties.getRealm());

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("client_secret", keycloakProperties.getClientSecret()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "Failed to obtain client credentials token: " + body))))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<>() {});
    }
}
