/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.cache.annotation;

import io.microsphere.spring.cache.redis.ConfigurableRedisCacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * {@link TTLCacheable} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        TTLCacheableTest.class, TTLCacheableTest.CachingData.class
})
@EnableTTLCaching(proxyTargetClass = true)
public class TTLCacheableTest {

    @Bean
    public static CacheManager simpleCacheManager() {
        Collection<? extends Cache> caches = singletonList(new ConcurrentMapCache("test"));
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(caches);
        return simpleCacheManager;
    }

    @Bean
    @Primary
    public static CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        return ConfigurableRedisCacheManager.create(redisConnectionFactory);
    }

    @Bean(destroyMethod = "destroy")
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", 6379));
    }


    static class CachingData {
        @TTLCacheable(cacheNames = "test",
                cacheManagers = {"redisCacheManager"},
                expire = 100)
        public int randomInt(int value) {
            return ThreadLocalRandom.current().nextInt(value);
        }
    }

    @Autowired
    private CachingData testData;

    @Test
    public void test() throws InterruptedException {
        int value = testData.randomInt(100);
        assertEquals(value, testData.randomInt(100));

        Thread.sleep(200L);

        assertNotEquals(value, testData.randomInt(100));
    }
}
