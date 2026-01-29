package com.company.security.shared.infrastructure.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        String finalCorrelationId = correlationId;
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(CORRELATION_ID_MDC_KEY, finalCorrelationId))
                .doFirst(() -> MDC.put(CORRELATION_ID_MDC_KEY, finalCorrelationId))
                .doFinally(signalType -> MDC.remove(CORRELATION_ID_MDC_KEY));
    }
}
