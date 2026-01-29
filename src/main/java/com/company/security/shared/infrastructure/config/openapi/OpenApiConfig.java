package com.company.security.shared.infrastructure.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Security Service API")
                        .version("1.0.0")
                        .description("API for authentication, password management, and token operations"))
                .tags(List.of(
                        new Tag().name("Authentication").description("Authentication operations"),
                        new Tag().name("Password Management").description("Password management operations"),
                        new Tag().name("Token - Internal").description("Internal token validation operations")
                ));
    }
}
