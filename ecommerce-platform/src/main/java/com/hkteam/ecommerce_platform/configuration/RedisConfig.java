package com.hkteam.ecommerce_platform.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.spring.redis.host:localhost}") // default localhost if env variable not set
    String host;

    @Value("${spring.spring.redis.port:6379}") // default 6379 if env variable not set
    int port;

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {
        // Config for the default cache behavior
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // Default TTL for all caches
                .disableCachingNullValues() // Don't cache null values
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis connection factory for Lettuce
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Create the cache manager with default configuration
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfiguration())
                .withCacheConfiguration("productCache", createCacheConfigWithTtl(2)) // Custom TTL for specific caches
                .withCacheConfiguration("searchCache", createCacheConfigWithTtl(1))
                .withCacheConfiguration("autoSuggestCache", createCacheConfigWithTtl(2))
                .withCacheConfiguration("userCache", createCacheConfigWithTtl(30))
                .withCacheConfiguration("categoryCache", createCacheConfigWithTtl(5))
                .withCacheConfiguration("categoriesTreeCache", createCacheConfigWithTtl(5))
                .build();
    }

    private RedisCacheConfiguration createCacheConfigWithTtl(long ttlMinutes) {
        // Create cache configuration with a specific TTL for a given cache
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(ttlMinutes))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(createObjectMapper())));
    }

    private ObjectMapper createObjectMapper() {
        // Create and configure the custom ObjectMapper for JSON serialization
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}
