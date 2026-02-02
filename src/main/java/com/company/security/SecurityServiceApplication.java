package com.company.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SecurityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityServiceApplication.class, args);
    }
}
