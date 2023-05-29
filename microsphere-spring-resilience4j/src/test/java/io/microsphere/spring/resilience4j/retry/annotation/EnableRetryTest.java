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
package io.microsphere.spring.resilience4j.retry.annotation;

import io.microsphere.spring.core.convert.annotation.EnableSpringConverterAdapter;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.configure.RetryConfigurationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * {@link EnableRetry} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EnableRetryTest.class})
@TestPropertySource(properties = {
        "microsphere.resilience4j.retry.instances[test].waitDuration=PT1S",
        "microsphere.resilience4j.retry.instances[test].maxAttempts=1",
        "microsphere.resilience4j.retry.instances[test].eventConsumerBufferSize=99"})
@EnableRetry
@EnableSpringConverterAdapter
public class EnableRetryTest {

    @Autowired
    private RetryRegistry registry;

    @Autowired
    private RetryConfigurationProperties properties;

    @Test
    public void test() {
        Retry retry = registry.retry("test");

        RetryConfigurationProperties.InstanceProperties instanceProperties = properties.getInstances().get("test");
        assertEquals(Duration.ofSeconds(1), instanceProperties.getWaitDuration());
        assertEquals(Integer.valueOf(1), instanceProperties.getMaxAttempts());
        assertEquals(Integer.valueOf(99), instanceProperties.getEventConsumerBufferSize());

        retry.executeSupplier(() -> "Hello,World");
    }
}
