package com.capg.jobservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisConfigTest {

    @Test
    void cacheManager_isNotNull() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConfig config = new RedisConfig();
        RedisCacheManager manager = config.cacheManager(factory);
        assertNotNull(manager);
    }
}
