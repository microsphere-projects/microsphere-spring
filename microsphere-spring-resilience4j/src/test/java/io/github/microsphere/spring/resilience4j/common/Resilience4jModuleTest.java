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
package io.github.microsphere.spring.resilience4j.common;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.Registry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.Test;

import java.io.Serializable;

import static io.github.microsphere.spring.resilience4j.common.Resilience4jModule.BULKHEAD;
import static io.github.microsphere.spring.resilience4j.common.Resilience4jModule.CIRCUIT_BREAKER;
import static io.github.microsphere.spring.resilience4j.common.Resilience4jModule.RATE_LIMITER;
import static io.github.microsphere.spring.resilience4j.common.Resilience4jModule.RETRY;
import static io.github.microsphere.spring.resilience4j.common.Resilience4jModule.TIME_LIMITER;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.ofDefaults;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link Resilience4jModule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Resilience4jModuleTest {

    @Test
    public void test() {
        assertModule(CIRCUIT_BREAKER, CircuitBreaker.class, CircuitBreakerRegistry.class, CircuitBreakerConfig.class, 1);
        assertModule(BULKHEAD, Bulkhead.class, BulkheadRegistry.class, BulkheadConfig.class, 4);
        assertModule(RATE_LIMITER, RateLimiter.class, RateLimiterRegistry.class, RateLimiterConfig.class, 3);
        assertModule(RETRY, Retry.class, RetryRegistry.class, RetryConfig.class, 0);
        assertModule(TIME_LIMITER, TimeLimiter.class, TimeLimiterRegistry.class, TimeLimiterConfig.class, 3);
    }

    @Test
    public void testGetEntry() {
        CircuitBreaker circuitBreaker = CIRCUIT_BREAKER.getEntry(ofDefaults(), "test");
        assertNotNull(circuitBreaker);
    }

    private void assertModule(Resilience4jModule module, Class<?> entryClass, Class<? extends Registry> registryClass,
                              Class<? extends Serializable> configClass, int defaultAspectOrder) {
        assertEquals(entryClass, module.getEntryClass());
        assertEquals(registryClass, module.getRegistryClass());
        assertEquals(configClass, module.getConfigurationClass());
        assertEquals(defaultAspectOrder, module.getDefaultAspectOrder());
    }
}
