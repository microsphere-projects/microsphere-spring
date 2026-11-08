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
package io.microsphere.spring.context.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link DefaultConfigurationBeanBinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultConfigurationBeanBinder
 * @since 1.0.0
 */
class DefaultConfigurationBeanBinderTest {

    private DefaultConfigurationBeanBinder configurationBeanBinder;

    @BeforeEach
    void setUp() {
        this.configurationBeanBinder = new DefaultConfigurationBeanBinder();
        this.configurationBeanBinder.setConversionService(new DefaultConversionService());
    }

    @Test
    void testBindKnownProperty() {
        TestConfigurationBean configurationBean = new TestConfigurationBean();

        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put("name", "test");
        configurationProperties.put("count", "42");

        this.configurationBeanBinder.bind(configurationProperties, true, false, configurationBean);

        assertEquals("test", configurationBean.getName());
        assertEquals(42, configurationBean.getCount());
    }

    /**
     * The behavior of unknown fields must be controlled by the {@code ignoreUnknownFields}
     * argument, NOT the {@code ignoreInvalidFields} argument
     */
    @Test
    void testIgnoreUnknownFields() {
        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put("unknownProperty", "value");

        // ignoreUnknownFields = true : unknown fields are tolerated
        assertDoesNotThrow(() ->
                this.configurationBeanBinder.bind(configurationProperties, true, false, new TestConfigurationBean()));

        // ignoreUnknownFields = false : unknown fields must fail the binding
        assertThrows(RuntimeException.class, () ->
                this.configurationBeanBinder.bind(configurationProperties, false, true, new TestConfigurationBean()));
    }

    /**
     * The behavior of invalid fields (null value in a nested property path) must be
     * controlled by the {@code ignoreInvalidFields} argument, NOT the
     * {@code ignoreUnknownFields} argument
     */
    @Test
    void testIgnoreInvalidFields() {
        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put("nested.name", "test"); // 'nested' is null on the target bean

        // ignoreInvalidFields = true : null-value-in-nested-path is tolerated
        assertDoesNotThrow(() ->
                this.configurationBeanBinder.bind(configurationProperties, true, true, new TestConfigurationBean()));

        // ignoreInvalidFields = false : null-value-in-nested-path must fail the binding
        assertThrows(RuntimeException.class, () ->
                this.configurationBeanBinder.bind(configurationProperties, true, false, new TestConfigurationBean()));
    }

    private static class TestConfigurationBean {

        private String name;

        private int count;

        private Nested nested;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Nested getNested() {
            return nested;
        }

        public void setNested(Nested nested) {
            this.nested = nested;
        }
    }

    private static class Nested {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
