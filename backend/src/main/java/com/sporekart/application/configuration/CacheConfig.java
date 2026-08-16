package com.sporekart.application.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Sprint 6E — Performance Hardening.
 *
 * Configures Spring Cache Manager for read-heavy, low-volatility domain data:
 * - `categories`: Catalog categories list & detail lookup
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_CATEGORIES = "categories";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(CACHE_CATEGORIES));
        return cacheManager;
    }
}
