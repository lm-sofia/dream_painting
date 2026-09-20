package com.aiproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * 缓存配置：同一套 @Cacheable 业务代码，缓存后端按环境可插拔。
 * - 生产/默认 profile：Redis（多实例共享，TTL 1 小时防无限膨胀）
 * - test profile：进程内 ConcurrentHashMap（零依赖，跑测试不连 Redis）
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final Duration STYLE_TTL = Duration.ofHours(1);

    /** 生产：Redis 缓存（含 Instant 等 JSR310 时间类型 + 多态类型信息） */
    @Bean
    @Profile("!test")
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        // JSON 序列化：value 存为可读 JSON（默认 JDK 序列化二进制，排障困难且不跨语言）
        // 两个必须项（第 6 课 CI 实战坑）：
        // 1) JavaTimeModule：DTO 含 java.time.Instant，默认 ObjectMapper 不支持 → 缓存写入 500
        // 2) activateDefaultTyping：缓存 value 是 List<StyleResponse> 泛型，无类型信息反序列化失败
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(STYLE_TTL)
                .disableCachingNullValues() // 禁止缓存 null：防缓存穿透的第一道闸
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(om)));
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    @Bean
    @Profile("test")
    public CacheManager simpleCacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
