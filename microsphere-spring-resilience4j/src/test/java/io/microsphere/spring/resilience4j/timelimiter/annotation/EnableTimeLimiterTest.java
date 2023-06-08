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
package io.microsphere.spring.resilience4j.timelimiter.annotation;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.configure.TimeLimiterConfigurationProperties;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnSuccessEvent;
import io.microsphere.spring.core.convert.annotation.EnableSpringConverterAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * {@link EnableTimeLimiter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EnableTimeLimiterTest.class})
@TestPropertySource(properties = {
        "microsphere.resilience4j.timelimiter.instances[test].timeoutDuration=PT10S",
        "microsphere.resilience4j.timelimiter.instances[test].cancelRunningFuture=true",
        "microsphere.resilience4j.timelimiter.instances[test].eventConsumerBufferSize=200"})
@EnableTimeLimiter
@EnableSpringConverterAdapter
public class EnableTimeLimiterTest {

    @Autowired
    private TimeLimiterRegistry registry;

    @Autowired
    private TimeLimiterConfigurationProperties properties;

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    @Test
    public void test() {
        TimeLimiter timeLimiter = registry.timeLimiter("test");

        TimeLimiterConfigurationProperties.InstanceProperties instanceProperties = properties.getInstances().get("test");
        assertEquals(Duration.ofSeconds(10), instanceProperties.getTimeoutDuration());
        assertEquals(Boolean.TRUE, instanceProperties.getCancelRunningFuture());
        assertEquals(Integer.valueOf(200), instanceProperties.getEventConsumerBufferSize());

        timeLimiter.onSuccess();
    }

    @EventListener(TimeLimiterOnSuccessEvent.class)
    public void onTimeLimiterOnSuccessEvent(TimeLimiterOnSuccessEvent event) {
        assertEquals("test", event.getTimeLimiterName());
    }
}
