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

package io.microsphere.spring.core.io.support;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static io.microsphere.spring.core.io.support.PropertiesUtils.loadProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PropertiesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertiesUtils
 * @since 1.0.0
 */
class PropertiesUtilsTest {

    static final String PROPERTIES = """
            a = 1
            b : 2
            c 3
            """;

    @Test
    void testLoadProperties() throws IOException {
        Properties properties = loadProperties("a=1", "b : 2", "c 3");
        assertProperties(properties);
    }

    @Test
    void testLoadPropertiesOnTextBlock() throws IOException {
        Properties properties = loadProperties(PROPERTIES);
        assertProperties(properties);
    }

    void assertProperties(Properties properties) {
        assertEquals(3, properties.size());
        assertEquals("1", properties.getProperty("a"));
        assertEquals("2", properties.getProperty("b"));
        assertEquals("3", properties.getProperty("c"));
    }
}