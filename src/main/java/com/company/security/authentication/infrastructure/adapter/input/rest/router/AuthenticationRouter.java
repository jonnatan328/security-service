package com.company.security.authentication.infrastructure.adapter.input.rest.router;

import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.RefreshTokenRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.handler.AuthenticationHandler;
import com.company.security.shared.infrastructure.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * Router configuration for authentication endpoints.
 */
@Configuration
public class AuthenticationRouter {

    private static final String BASE_PATH = "/api/v1/auth";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = BASE_PATH + "/signin",
                    method = RequestMethod.POST,
                    beanClass = AuthenticationHandler.class,
                    beanMethod = "signIn",
                    operation = @Operation(
                            operationId = "signIn",
                            summary = "User sign-in",
                            description = "Authenticates a user against the directory service and returns access/refresh tokens",
                            tags = {"Authentication"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = SignInRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Authentication successful",
                                            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Invalid credentials",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "423",
                                            description = "Account locked",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/signout",
                    method = RequestMethod.POST,
                    beanClass = AuthenticationHandler.class,
                    beanMethod = "signOut",
                    operation = @Operation(
                            operationId = "signOut",
                            summary = "User sign-out",
                            description = "Signs out the user by invalidating access and refresh tokens",
                            tags = {"Authentication"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "204",
                                            description = "Sign-out successful"
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/refresh",
                    method = RequestMethod.POST,
                    beanClass = AuthenticationHandler.class,
                    beanMethod = "refresh",
                    operation = @Operation(
                            operationId = "refreshToken",
                            summary = "Refresh access token",
                            description = "Exchanges a valid refresh token for a new access/refresh token pair",
                            tags = {"Authentication"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Token refresh successful",
                                            content = @Content(schema = @Schema(implementation = TokenResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Invalid or expired refresh token",
                                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> authenticationRoutes(AuthenticationHandler handler) {
        return RouterFunctions.route()
                .POST(BASE_PATH + "/signin", accept(MediaType.APPLICATION_JSON), handler::signIn)
                .POST(BASE_PATH + "/signout", handler::signOut)
                .POST(BASE_PATH + "/refresh", accept(MediaType.APPLICATION_JSON), handler::refresh)
                .build();
    }
}
