package com.company.security.password.infrastructure.adapter.output.client;

import com.company.security.password.infrastructure.adapter.output.client.dto.UserInfoClientResponse;
import com.company.security.password.domain.port.output.UserLookupPort;
import com.company.security.shared.infrastructure.properties.ServicesProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ClientServiceAdapter implements UserLookupPort {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceAdapter.class);

    private final WebClient webClient;

    public ClientServiceAdapter(WebClient.Builder webClientBuilder, ServicesProperties servicesProperties) {
        this.webClient = webClientBuilder
                .baseUrl(servicesProperties.getClientService().getBaseUrl())
                .build();
    }

    @Override
    @CircuitBreaker(name = "clientService", fallbackMethod = "findByEmailFallback")
    @Retry(name = "clientService")
    public Mono<UserInfo> findByEmail(String email) {
        log.debug("Looking up user by email via Client Service: {}", email);

        return webClient.get()
                .uri("/api/v1/users/by-email?email={email}", email)
                .retrieve()
                .bodyToMono(UserInfoClientResponse.class)
                .map(response -> new UserInfo(response.userId(), response.email(), response.username()))
                .doOnNext(user -> log.debug("User found: {}", user.userId()))
                .doOnError(e -> log.error("Failed to lookup user by email: {}", email, e));
    }

    private Mono<UserInfo> findByEmailFallback(String email, Throwable t) {
        log.error("Circuit breaker open for Client Service lookup, email: {}", email, t);
        return Mono.empty();
    }
}
