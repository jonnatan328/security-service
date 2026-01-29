package com.company.security.shared.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration = 900;
    private long refreshTokenExpiration = 604800;
    private String issuer;
}
