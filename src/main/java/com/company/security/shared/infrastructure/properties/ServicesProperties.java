package com.company.security.shared.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "services")
public class ServicesProperties {

    private ServiceConfig clientService;

    @Data
    public static class ServiceConfig {

        private String baseUrl;
        private int timeout = 5000;
    }
}
