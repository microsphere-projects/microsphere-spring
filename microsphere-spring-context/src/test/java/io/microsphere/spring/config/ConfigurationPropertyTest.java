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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        ConfigurationProperty property = new ConfigurationProperty(name);
        property.setValue(value);
        property.setDefaultValue(defaultValue);
        property.setType(type);

        assertEquals(property, property);
        assertEquals(name, property.getName());
        assertEquals(type, property.getType());
        assertEquals(value, property.getValue());
        assertEquals(defaultValue, property.getDefaultValue());
        assertFalse(property.isRequired());
        assertNotNull(property.getMetadata());
    }

    @Test
    public void testNull() {
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty(null));
        assertThrows(IllegalArgumentException.class, () -> new ConfigurationProperty("test").setType(null));
    }
}
