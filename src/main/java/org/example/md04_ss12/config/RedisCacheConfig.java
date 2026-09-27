package org.example.md04_ss12.config;

import org.example.md04_ss12.product.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Bai 1: Spring Cache + Redis (Cache-aside).
 * - Key dang String:  product-service::products::{id}
 * - Value dang JSON (doc duoc bang redis-cli, khong phu thuoc Java serialization)
 * - TTL 30 phut de Redis khong bi day boi san pham cu
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String PRODUCT_CACHE = "products";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                          @Value("${app.cache.product-ttl:30m}") Duration productTtl) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("product-service::")
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));

        RedisCacheConfiguration productCache = defaults
                .entryTtl(productTtl)
                .serializeValuesWith(SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(ProductResponse.class)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration(PRODUCT_CACHE, productCache)
                .build();
    }
}
