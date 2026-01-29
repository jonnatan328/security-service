package com.company.security.password.infrastructure.adapter.input.rest.router;

import com.company.security.password.infrastructure.adapter.input.rest.handler.PasswordHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class PasswordRouter {

    private static final String BASE_PATH = "/api/v1/password";

    @Bean
    public RouterFunction<ServerResponse> passwordRoutes(PasswordHandler handler) {
        return RouterFunctions.route()
                .POST(BASE_PATH + "/recover", accept(MediaType.APPLICATION_JSON), handler::recoverPassword)
                .POST(BASE_PATH + "/reset", accept(MediaType.APPLICATION_JSON), handler::resetPassword)
                .POST(BASE_PATH + "/update", accept(MediaType.APPLICATION_JSON), handler::updatePassword)
                .build();
    }
}
