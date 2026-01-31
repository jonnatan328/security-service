package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.exception.AccountDisabledException;
import com.company.security.authentication.domain.exception.DirectoryServiceException;
import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.port.output.DirectoryServicePort;
import com.company.security.shared.infrastructure.properties.KeycloakProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;

/**
 * Keycloak Directory Service adapter implementation.
 * Provides authentication against Keycloak via ROPC grant (Resource Owner Password Credentials).
 */
public class KeycloakDirectoryAdapter implements DirectoryServicePort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakDirectoryAdapter.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;
    private final KeycloakUserMapper userMapper;
    private final ObjectMapper objectMapper;

    public KeycloakDirectoryAdapter(
            WebClient.Builder webClientBuilder,
            KeycloakProperties keycloakProperties,
            KeycloakUserMapper userMapper,
            ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl(keycloakProperties.getServerUrl())
                .build();
        this.keycloakProperties = keycloakProperties;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "keycloakService", fallbackMethod = "authenticateFallback")
    @Retry(name = "keycloakService")
    @TimeLimiter(name = "keycloakService")
    public Mono<AuthenticatedUser> authenticate(Credentials credentials) {
        log.debug("Authenticating user via Keycloak: {}", credentials.username());

        return requestToken(credentials.username(), credentials.password())
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    if (accessToken == null) {
                        return Mono.error(new DirectoryServiceException("No access_token in Keycloak response"));
                    }

                    // Decode token payload to extract roles
                    Map<String, Object> tokenClaims = decodeTokenPayload(accessToken);

                    // Fetch userinfo for user details
                    return fetchUserInfo(accessToken)
                            .map(userInfoClaims -> userMapper.map(tokenClaims, userInfoClaims));
                });
    }

    @Override
    @CircuitBreaker(name = "keycloakService", fallbackMethod = "findByUsernameFallback")
    @Retry(name = "keycloakService")
    public Mono<AuthenticatedUser> findByUsername(String username) {
        log.debug("Looking up user via Keycloak Admin API: {}", username);

        return requestClientCredentialsToken()
                .flatMap(tokenResponse -> {
                    String accessToken = (String) tokenResponse.get("access_token");
                    if (accessToken == null) {
                        return Mono.<AuthenticatedUser>error(
                                new DirectoryServiceException("No access_token in client credentials response"));
                    }

                    return fetchUserByUsername(accessToken, username)
                            .flatMap(userData -> fetchUserRealmRoles(accessToken, (String) userData.get("id"))
                                    .map(roles -> mapAdminUserToAuthenticatedUser(userData, roles)));
                });
    }

    @Override
    public Mono<Boolean> isAvailable() {
        return webClient.get()
                .uri("/realms/{realm}", keycloakProperties.getRealm())
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
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
                        .with("password", password)
                        .with("scope", "openid profile email"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> handleTokenErrorResponse(body, username)))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

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
                                .flatMap(body -> Mono.error(new DirectoryServiceException(
                                        "Failed to obtain client credentials token: " + body))))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    private Mono<Map<String, Object>> fetchUserInfo(String accessToken) {
        String userInfoUri = String.format("/realms/%s/protocol/openid-connect/userinfo",
                keycloakProperties.getRealm());

        return webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new DirectoryServiceException(
                                        "Failed to fetch userinfo from Keycloak"))))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    private Mono<Map<String, Object>> fetchUserByUsername(String accessToken, String username) {
        String usersUri = String.format("/admin/realms/%s/users", keycloakProperties.getRealm());

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(usersUri)
                        .queryParam("username", username)
                        .queryParam("exact", true)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new DirectoryServiceException(
                                        "Failed to fetch user from Keycloak Admin API"))))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.List<Map<String, Object>>>() {})
                .flatMap(users -> {
                    if (users == null || users.isEmpty()) {
                        return Mono.error(new DirectoryServiceException(
                                "User not found in Keycloak: " + username));
                    }
                    return Mono.just(users.get(0));
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<java.util.List<Map<String, Object>>> fetchUserRealmRoles(String accessToken, String userId) {
        String rolesUri = String.format("/admin/realms/%s/users/%s/role-mappings/realm",
                keycloakProperties.getRealm(), userId);

        return webClient.get()
                .uri(rolesUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .then(Mono.empty()))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.List<Map<String, Object>>>() {})
                .onErrorReturn(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private AuthenticatedUser mapAdminUserToAuthenticatedUser(
            Map<String, Object> userData, java.util.List<Map<String, Object>> realmRoles) {

        String userId = (String) userData.get("id");
        String username = (String) userData.get("username");
        String email = (String) userData.get("email");
        String firstName = (String) userData.get("firstName");
        String lastName = (String) userData.get("lastName");
        Boolean enabled = (Boolean) userData.get("enabled");

        java.util.Set<String> roles = realmRoles.stream()
                .map(role -> (String) role.get("name"))
                .filter(name -> name != null && (name.startsWith("APP_") || name.startsWith("ROLE_")))
                .map(name -> name.startsWith("APP_") ? "ROLE_" + name.substring(4) : name)
                .collect(java.util.stream.Collectors.toSet());

        String effectiveEmail = email != null ? email : username + "@unknown.local";

        return AuthenticatedUser.builder()
                .userId(userId)
                .username(username)
                .email(effectiveEmail)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .groups(Collections.emptySet())
                .enabled(enabled != null ? enabled : true)
                .build();
    }

    Map<String, Object> decodeTokenPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return Collections.emptyMap();
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(decodedBytes, MAP_TYPE_REF);
        } catch (Exception e) {
            log.warn("Failed to decode Keycloak access token payload", e);
            return Collections.emptyMap();
        }
    }

    private Mono<Throwable> handleTokenErrorResponse(String body, String username) {
        try {
            Map<String, Object> errorMap = objectMapper.readValue(body, MAP_TYPE_REF);
            String error = (String) errorMap.get("error");
            String errorDescription = (String) errorMap.get("error_description");

            if ("invalid_grant".equals(error)) {
                if (errorDescription != null && errorDescription.toLowerCase().contains("account disabled")) {
                    log.warn("Keycloak authentication failed for user: {} - Account disabled", username);
                    return Mono.just(new AccountDisabledException(username));
                }
                log.warn("Keycloak authentication failed for user: {} - Invalid credentials", username);
                return Mono.just(new InvalidCredentialsException(username));
            }

            log.error("Keycloak token request error: {} - {}", error, errorDescription);
            return Mono.just(new DirectoryServiceException(
                    "Keycloak authentication error: " + error + " - " + errorDescription));

        } catch (Exception e) {
            log.error("Failed to parse Keycloak error response: {}", body, e);
            return Mono.just(new DirectoryServiceException("Keycloak authentication failed", e));
        }
    }

    // Fallback methods for circuit breaker
    private Mono<AuthenticatedUser> authenticateFallback(Credentials credentials, Throwable t) {
        if (t instanceof InvalidCredentialsException || t instanceof AccountDisabledException) {
            return Mono.error(t);
        }
        log.error("Circuit breaker open for Keycloak authentication, failing request for user: {}",
                credentials.username(), t);
        return Mono.error(new DirectoryServiceException("Directory service temporarily unavailable", t));
    }

    private Mono<AuthenticatedUser> findByUsernameFallback(String username, Throwable t) {
        log.error("Circuit breaker open for Keycloak lookup, failing request for user: {}", username, t);
        return Mono.error(new DirectoryServiceException("Directory service temporarily unavailable", t));
    }
}
