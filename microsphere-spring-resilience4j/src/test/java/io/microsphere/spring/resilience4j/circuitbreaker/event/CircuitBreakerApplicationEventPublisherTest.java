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
package io.microsphere.spring.resilience4j.circuitbreaker.event;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.configure.CircuitBreakerConfiguration;
import io.github.resilience4j.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnSuccessEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * {@link CircuitBreakerApplicationEventPublisher} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        CircuitBreakerConfigurationProperties.class,
        CircuitBreakerConfiguration.class,
        CircuitBreakerApplicationEventPublisher.class,
        CircuitBreakerApplicationEventPublisherTest.class})
public class CircuitBreakerApplicationEventPublisherTest {

    @Autowired
    private CircuitBreakerRegistry registry;

    @Test
    public void test() {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("test");
        circuitBreaker.acquirePermission();
        circuitBreaker.onSuccess(100, TimeUnit.MILLISECONDS);
    }

    @EventListener(CircuitBreakerOnSuccessEvent.class)
    public void onCircuitBreakerOnSuccessEvent(CircuitBreakerOnSuccessEvent event) {
        assertEquals("test", event.getCircuitBreakerName());
        assertEquals(Duration.ofMillis(100), event.getElapsedDuration());
    }
}
