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


import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static io.microsphere.spring.core.io.support.PropertiesUtils.loadProperties;
import static org.junit.Assert.assertEquals;

/**
 * {@link PropertiesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertiesUtils
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class PropertiesUtilsTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    static final String PROPERTIES = "a = 1\n" +
            "            b : 2\n" +
            "            c 3";

    @Test
    public void testLoadProperties() throws IOException {
        Properties properties = loadProperties("a=1", "b : 2", "c 3");
        assertProperties(properties);
    }

    @Test
    public void testLoadPropertiesOnTextBlock() throws IOException {
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