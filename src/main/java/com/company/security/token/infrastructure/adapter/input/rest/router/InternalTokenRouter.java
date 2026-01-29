package com.company.security.token.infrastructure.adapter.input.rest.router;

import com.company.security.token.infrastructure.adapter.input.rest.handler.TokenValidationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class InternalTokenRouter {

    @Bean
    public RouterFunction<ServerResponse> internalTokenRoutes(TokenValidationHandler handler) {
        return RouterFunctions.route()
                .POST("/internal/v1/token/validate", accept(MediaType.APPLICATION_JSON), handler::validate)
                .build();
    }
}
