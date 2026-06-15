package com.yb.common.redis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Cache 配置 - 基于 Redis 的缓存管理
 * <p>
 * 支持不同缓存名配置不同 TTL：
 * <ul>
 *   <li>product:detail → 30 分钟</li>
 *   <li>product:categories → 1 小时</li>
 *   <li>默认 → 10 分钟</li>
 * </ul>
 */
@AutoConfiguration
@EnableCaching
public class CacheConfig {

    /** 默认缓存过期时间（分钟） */
    public static final long DEFAULT_TTL_MINUTES = 10;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(DEFAULT_TTL_MINUTES))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer.UTF_8))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 按缓存名定制 TTL
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("product:detail",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        configMap.put("product:categories",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configMap)
                .transactionAware()
                .build();
    }
}
