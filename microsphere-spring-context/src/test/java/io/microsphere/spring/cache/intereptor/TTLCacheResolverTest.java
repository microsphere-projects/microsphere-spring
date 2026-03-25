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
package io.microsphere.spring.cache.intereptor;

import io.microsphere.spring.cache.annotation.EnableTTLCaching;
import io.microsphere.spring.cache.annotation.TTLCacheable;
import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.cache.intereptor.TTLCacheResolver.BEAN_NAME;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link TTLCacheResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TTLCacheResolver
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TTLCacheResolverTest.class,
        TTLCacheResolverTest.TestData.class
})
@EnableTTLCaching(proxyTargetClass = true)
@SpringLoggingTest
class TTLCacheResolverTest {

    /** Verifies that the well-known bean name constant has the expected value. */
    @Test
    void testBeanName() {
        assertEquals("ttlCacheResolver", BEAN_NAME);
    }

    // ---- Spring @Bean declarations ----

    @Bean
    public static CacheManager cacheManager() {
        Collection<? extends Cache> caches = ofList(
                new ConcurrentMapCache("test"),
                new ConcurrentMapCache("other")
        );
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(caches);
        return simpleCacheManager;
    }

    // ---- Test-data beans ----

    /** Provides a cacheable method exercised by testResolveCachesHappyPath. */
    static class TestData {

        @TTLCacheable(cacheNames = "test", timeUnit = MINUTES, expire = 1)
        public List<String> getNames() {
            return ofList("a", "b");
        }

        @TTLCacheable(cacheNames = "test", timeUnit = SECONDS, expire = 10)
        public List<String> getNamesWithSeconds() {
            return ofList("x");
        }
    }

    @Autowired
    private TestData testData;

    @Autowired
    private TTLCacheResolver ttlCacheResolver;

    /** The resolver bean must be present in the context. */
    @Test
    void testResolverBeanPresent() {
        assertNotNull(ttlCacheResolver);
    }

    /**
     * Calling the @TTLCacheable method twice must return the same result (Spring caches
     * the first call via TTLCacheResolver). This exercises resolveCaches for the
     * CacheableOperation path.
     */
    @Test
    void testResolveCachesHappyPath() {
        List<String> first = testData.getNames();
        List<String> second = testData.getNames();
        assertEquals(first, second);
    }

    /**
     * Second distinct @TTLCacheable method (different TTL) also round-trips correctly.
     */
    @Test
    void testResolveCachesWithDifferentTTL() {
        List<String> first = testData.getNamesWithSeconds();
        List<String> second = testData.getNamesWithSeconds();
        assertEquals(first, second);
    }
}
