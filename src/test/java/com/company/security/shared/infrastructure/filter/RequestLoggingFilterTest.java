package com.company.security.shared.infrastructure.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void filter_logsRequestAndResponse() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = e -> {
            e.getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_withPostRequest_logsCorrectly() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/signin").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = e -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }
}
