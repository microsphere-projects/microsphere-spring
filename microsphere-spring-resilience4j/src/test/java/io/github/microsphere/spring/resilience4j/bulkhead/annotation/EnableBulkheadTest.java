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
package io.github.microsphere.spring.resilience4j.bulkhead.annotation;

import io.github.microsphere.spring.core.convert.annotation.EnableSpringConverterAdapter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.configure.BulkheadConfigurationProperties;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

import static io.github.resilience4j.bulkhead.event.BulkheadEvent.Type.CALL_PERMITTED;
import static org.junit.Assert.assertEquals;

/**
 * {@link EnableBulkhead} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EnableBulkheadTest.class})
@TestPropertySource(properties = {
        "microsphere.resilience4j.bulkhead.instances[test].maxConcurrentCalls=10",
        "microsphere.resilience4j.bulkhead.instances[test].eventConsumerBufferSize=100",
        "microsphere.resilience4j.bulkhead.instances[test].maxWaitDuration=PT30S"})
@EnableBulkhead
@EnableSpringConverterAdapter
public class EnableBulkheadTest {

    @Autowired
    private BulkheadRegistry registry;

    @Autowired
    private BulkheadConfigurationProperties properties;

    @Test
    public void test() {
        Bulkhead circuitBreaker = registry.bulkhead("test");
        circuitBreaker.acquirePermission();

        io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigurationProperties.InstanceProperties instanceProperties = properties.getInstances().get("test");
        assertEquals(Integer.valueOf(10), instanceProperties.getMaxConcurrentCalls());
        assertEquals(Integer.valueOf(100), instanceProperties.getEventConsumerBufferSize());
        assertEquals(Duration.ofSeconds(30), instanceProperties.getMaxWaitDuration());
    }

    @EventListener(BulkheadOnCallPermittedEvent.class)
    public void onBulkheadOnCallPermittedEvent(BulkheadOnCallPermittedEvent event) {
        assertEquals("test", event.getBulkheadName());
        assertEquals(CALL_PERMITTED, event.getEventType());
    }
}
