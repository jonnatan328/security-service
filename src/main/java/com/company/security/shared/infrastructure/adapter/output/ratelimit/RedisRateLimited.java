package com.company.security.shared.infrastructure.adapter.output.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisRateLimited {
    String keyPrefix();
    int maxRequests();
    long windowSeconds();
    String keyParamName();
}
