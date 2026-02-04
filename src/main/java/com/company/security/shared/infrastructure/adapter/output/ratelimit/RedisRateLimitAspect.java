package com.company.security.shared.infrastructure.adapter.output.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Aspect
public class RedisRateLimitAspect {

    private final RateLimitingRedisAdapter rateLimitingAdapter;

    public RedisRateLimitAspect(RateLimitingRedisAdapter rateLimitingAdapter) {
        this.rateLimitingAdapter = rateLimitingAdapter;
    }

    @Around("@annotation(redisRateLimited)")
    public Object around(ProceedingJoinPoint joinPoint, RedisRateLimited redisRateLimited) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String keyValue = resolveKeyValue(parameterNames, args, redisRateLimited.keyParamName());
        String redisKey = redisRateLimited.keyPrefix() + keyValue;
        Duration window = Duration.ofSeconds(redisRateLimited.windowSeconds());

        return rateLimitingAdapter.checkRateLimit(redisKey, redisRateLimited.maxRequests(), window)
                .then(Mono.defer(() -> {
                    try {
                        return (Mono<?>) joinPoint.proceed();
                    } catch (Throwable e) {
                        return Mono.error(e);
                    }
                }));
    }

    private String resolveKeyValue(String[] parameterNames, Object[] args, String keyParamName) {
        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals(keyParamName)) {
                return String.valueOf(args[i]);
            }
        }
        throw new IllegalArgumentException(
                "Parameter '" + keyParamName + "' not found in method signature");
    }
}
