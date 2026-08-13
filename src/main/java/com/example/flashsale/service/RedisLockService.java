package com.example.flashsale.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T withLock(String lockKey, Supplier<T> action) {
        String uniqueValue = UUID.randomUUID().toString();
        boolean acquired = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, uniqueValue, Duration.ofSeconds(5))
        );

        if (!acquired) {
            throw new IllegalStateException("Could not acquire lock for key: " + lockKey);
        }

        try {
            return action.get();
        } finally {
            if (uniqueValue.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
