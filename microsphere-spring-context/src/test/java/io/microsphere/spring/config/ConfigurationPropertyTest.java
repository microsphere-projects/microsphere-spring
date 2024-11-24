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
package io.microsphere.spring.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ConfigurationProperty} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class ConfigurationPropertyTest {

    @Test
    public void test() {
        String name = "test-name";
        String value = "test-value";
        String defaultValue = "default-value";
        Class<?> type = String.class;
        ConfigurationProperty descriptor = new ConfigurationProperty(name);
        descriptor.setValue(value);
        descriptor.setDefaultValue(defaultValue);
        descriptor.setType(type);

        assertEquals(descriptor, descriptor);
        assertEquals(name, descriptor.getName());
        assertEquals(value, descriptor.getValue());
        assertEquals(defaultValue, descriptor.getDefaultValue());
        assertEquals(type, descriptor.getType());

    }

    @Test
    public void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty(null));
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty("test").setType(null));
    }
}
