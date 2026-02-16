package com.company.security.shared.infrastructure.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void filter_withExistingCorrelationId_usesExistingId() {
        String existingId = "existing-correlation-id";
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = e -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .isEqualTo(existingId);
    }

    @Test
    void filter_withoutCorrelationId_generatesNewId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = e -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .isNotBlank();
    }

    @Test
    void filter_withBlankCorrelationId_generatesNewId() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = e -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        String generatedId = exchange.getResponse().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(generatedId).isNotBlank().isNotEqualTo("   ");
    }
}
