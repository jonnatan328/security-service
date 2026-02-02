package com.company.security.password.infrastructure.adapter.input.rest.controller;

import com.company.security.password.infrastructure.adapter.input.rest.dto.request.RecoverPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.ResetPasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.request.UpdatePasswordRequest;
import com.company.security.password.infrastructure.adapter.input.rest.dto.response.PasswordOperationResponse;
import com.company.security.password.infrastructure.adapter.input.rest.handler.PasswordHandler;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/password")
@ConditionalOnExpression("'${auth.provider:ldap}' != 'keycloak'")
public class PasswordController {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final PasswordHandler handler;

    public PasswordController(PasswordHandler handler) {
        this.handler = handler;
    }

    @PostMapping(value = "/recover", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PasswordOperationResponse>> recoverPassword(
            @Valid @RequestBody RecoverPasswordRequest request,
            ServerHttpRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst(HttpHeaders.USER_AGENT);

        return handler.recoverPassword(request, ipAddress, userAgent)
                .map(response -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response));
    }

    @PostMapping(value = "/reset", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PasswordOperationResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            ServerHttpRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst(HttpHeaders.USER_AGENT);

        return handler.resetPassword(request, ipAddress, userAgent)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PasswordOperationResponse>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Principal principal,
            ServerHttpRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst(HttpHeaders.USER_AGENT);

        return handler.updatePassword(request, principal.getName(), ipAddress, userAgent)
                .map(ResponseEntity::ok);
    }

    private String extractIpAddress(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst(X_FORWARDED_FOR))
                .map(header -> header.split(",")[0].trim())
                .orElseGet(() -> {
                    var remoteAddress = request.getRemoteAddress();
                    return remoteAddress != null
                            ? remoteAddress.getAddress().getHostAddress()
                            : "unknown";
                });
    }
}
