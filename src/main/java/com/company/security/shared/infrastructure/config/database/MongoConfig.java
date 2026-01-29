package com.company.security.shared.infrastructure.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoAuditing
@EnableReactiveMongoRepositories(basePackages = "com.company.security")
public class MongoConfig {
}
