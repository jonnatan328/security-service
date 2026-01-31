package com.company.security.shared.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private int connectionTimeout = 5000;
    private int readTimeout = 5000;
    private RoleMapping roleMapping = new RoleMapping();

    @Data
    public static class RoleMapping {
        private boolean useRealmRoles = true;
        private boolean useClientRoles = true;
        private String clientIdForRoles;
    }
}
