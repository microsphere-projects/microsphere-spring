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
package io.github.microsphere.spring.redis.annotation;

import io.github.microsphere.spring.redis.AbstractRedisTest;
import io.github.microsphere.spring.redis.config.RedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link EnableRedisConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConfiguration
 * @since 1.0.0
 */
@ContextConfiguration(classes = EnableRedisConfigurationTest.class)
@TestPropertySource(properties = {"microsphere.redis.enabled=true", "spring.application.name=test-app"})
@EnableRedisConfiguration
public class EnableRedisConfigurationTest extends AbstractRedisTest {

    @Autowired
    private RedisConfiguration redisConfiguration;

    @Autowired
    private Environment environment;

    @Test
    public void test() throws Throwable {
        assertSame(environment, redisConfiguration.getEnvironment());
        assertEquals("test-app", redisConfiguration.getApplicationName());
        assertTrue(redisConfiguration.isEnabled());
        assertTrue(redisConfiguration.isCommandEventExposed());
    }
}
