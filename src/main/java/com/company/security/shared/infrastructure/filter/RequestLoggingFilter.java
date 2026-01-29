package com.company.security.shared.infrastructure.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

        log.info("Incoming request: {} {} [correlationId={}]", method, path, correlationId);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    var status = exchange.getResponse().getStatusCode();
                    log.info("Completed request: {} {} -> {} [correlationId={}]",
                            method, path, status != null ? status.value() : "unknown", correlationId);
                });
    }
}
