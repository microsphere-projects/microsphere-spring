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
package io.microsphere.spring.core.env;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Map;

import static io.microsphere.spring.core.env.EnvironmentUtils.getConversionService;
import static io.microsphere.spring.core.env.EnvironmentUtils.getProperties;
import static io.microsphere.spring.core.env.EnvironmentUtils.resolveCommaDelimitedValueToList;
import static io.microsphere.spring.core.env.EnvironmentUtils.resolvePlaceholders;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link EnvironmentUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnvironmentUtils
 * @since 1.0.0
 */
public class EnvironmentUtilsTest {

    private ConfigurableEnvironment environment;

    @Before
    public void init() throws Exception {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("a", "1");
        mockEnvironment.setProperty("b", "2");
        mockEnvironment.setProperty("c", "3");
        this.environment = mockEnvironment;
    }

    @Test
    public void testGetProperties() {
        Map<String, String> properties = getProperties(environment);
        assertEquals(emptyMap(), properties);

        properties = getProperties(environment, "a");
        assertEquals(1, properties.size());
        assertEquals("1", properties.get("a"));
        assertNull(properties.get("b"));
        assertNull(properties.get("c"));

        properties = getProperties(environment, "a", "b");
        assertEquals(2, properties.size());
        assertEquals("1", properties.get("a"));
        assertEquals("2", properties.get("b"));
        assertNull(properties.get("c"));

        properties = getProperties(environment, "a", "b", "c");
        assertEquals(3, properties.size());
        assertEquals("1", properties.get("a"));
        assertEquals("2", properties.get("b"));
        assertEquals("3", properties.get("c"));
    }

    @Test
    public void testGetConversionService() {
        assertEquals(environment.getConversionService(), getConversionService(environment));
    }

    @Test
    public void testResolveCommaDelimitedValueToList() {
        List<String> list = resolveCommaDelimitedValueToList(environment, "${a},${b},${c}");
        assertEquals(3, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("3", list.get(2));
    }

    @Test
    public void testResolvePlaceholders() {
        Object value = resolvePlaceholders(environment, null, Integer.class);
        assertNull(value);

        value = resolvePlaceholders(environment, "${a}", Integer.class);
        assertEquals(Integer.valueOf(1), value);

        value = resolvePlaceholders(environment, "${d}", String.class, "default-value");
        assertEquals("${d}", value);
    }
}


