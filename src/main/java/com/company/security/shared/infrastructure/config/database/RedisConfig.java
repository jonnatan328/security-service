package com.company.security.shared.infrastructure.config.database;

import com.company.security.shared.infrastructure.adapter.output.ratelimit.RateLimitingRedisAdapter;
import com.company.security.shared.infrastructure.adapter.output.ratelimit.RedisRateLimitAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        RedisSerializer<String> serializer = new StringRedisSerializer();

        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(serializer)
                .key(serializer)
                .value(serializer)
                .hashKey(serializer)
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public RateLimitingRedisAdapter rateLimitingRedisAdapter(
            ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        return new RateLimitingRedisAdapter(reactiveRedisTemplate);
    }

    @Bean
    public RedisRateLimitAspect redisRateLimitAspect(RateLimitingRedisAdapter rateLimitingRedisAdapter) {
        return new RedisRateLimitAspect(rateLimitingRedisAdapter);
    }
}
