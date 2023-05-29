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
package io.microsphere.spring.resilience4j.ratelimiter.annotation;

import io.microsphere.spring.core.convert.annotation.EnableSpringConverterAdapter;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.configure.RateLimiterConfigurationProperties;
import io.github.resilience4j.ratelimiter.event.RateLimiterOnSuccessEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

import static io.github.resilience4j.ratelimiter.event.RateLimiterEvent.Type.SUCCESSFUL_ACQUIRE;
import static org.junit.Assert.assertEquals;

/**
 * {@link EnableRateLimiter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EnableRateLimiterTest.class})
@TestPropertySource(properties = {
        "microsphere.resilience4j.ratelimiter.instances[test].timeoutDuration=PT10S",
        "microsphere.resilience4j.ratelimiter.instances[test].limitRefreshPeriod=PT0.000001S",
        "microsphere.resilience4j.ratelimiter.instances[test].limitForPeriod=20"})
@EnableRateLimiter
@EnableSpringConverterAdapter
public class EnableRateLimiterTest {

    @Autowired
    private RateLimiterRegistry registry;

    @Autowired
    private RateLimiterConfigurationProperties properties;

    @Test
    public void test() {
        RateLimiter rateLimiter = registry.rateLimiter("test");
        rateLimiter.acquirePermission();

        RateLimiterConfigurationProperties.InstanceProperties instanceProperties = properties.getInstances().get("test");
        assertEquals(Duration.ofSeconds(10), instanceProperties.getTimeoutDuration());
        assertEquals(Integer.valueOf(20), instanceProperties.getLimitForPeriod());
        assertEquals(Duration.ofNanos(1000), instanceProperties.getLimitRefreshPeriod());

        rateLimiter.onSuccess();
    }

    @EventListener(RateLimiterOnSuccessEvent.class)
    public void onRateLimiterOnSuccessEvent(RateLimiterOnSuccessEvent event) {
        assertEquals("test", event.getRateLimiterName());
        assertEquals(SUCCESSFUL_ACQUIRE, event.getEventType());
    }
}
