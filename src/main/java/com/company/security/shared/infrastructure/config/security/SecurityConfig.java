package com.company.security.shared.infrastructure.config.security;

import com.company.security.authentication.domain.port.output.TokenBlacklistPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/signin",
            "/api/v1/auth/refresh",
            "/api/v1/password/recover",
            "/api/v1/password/reset",
            "/actuator/health/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/webjars/**"
    };

    private static final String[] AUTHENTICATED_PATHS = {
            "/api/v1/auth/signout",
            "/api/v1/password/update"
    };

    private static final String[] INTERNAL_PATHS = {
            "/internal/**"
    };

    private static final String ROLE_SERVICE = "SERVICE";

    @Bean
    public JwtAuthenticationManager jwtAuthenticationManager(
            TokenProviderPort tokenProviderPort,
            TokenBlacklistPort tokenBlacklistPort) {
        return new JwtAuthenticationManager(tokenProviderPort, tokenBlacklistPort);
    }

    @Bean
    public SecurityContextRepository securityContextRepository(
            JwtAuthenticationManager authenticationManager) {
        return new SecurityContextRepository(authenticationManager);
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new CustomAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler(ObjectMapper objectMapper) {
        return new CustomAccessDeniedHandler(objectMapper);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions
                                .mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                        .contentTypeOptions(Customizer.withDefaults())
                        .cache(Customizer.withDefaults())
                        .hsts(hsts -> hsts.includeSubdomains(true)
                                .maxAge(java.time.Duration.ofSeconds(31536000)))
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                )
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(securityContextRepository)
                .authenticationManager(authenticationManager)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .pathMatchers(INTERNAL_PATHS).hasRole(ROLE_SERVICE)
                        .pathMatchers(AUTHENTICATED_PATHS).authenticated()
                        .anyExchange().authenticated()
                )
                .build();
    }
}
