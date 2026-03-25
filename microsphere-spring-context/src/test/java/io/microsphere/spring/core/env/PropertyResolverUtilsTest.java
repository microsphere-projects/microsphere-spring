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


import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static io.microsphere.spring.core.env.PropertyResolverUtils.resolvePlaceholders;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link PropertyResolverUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyResolverUtils
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class PropertyResolverUtilsTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    private MockEnvironment environment;

    @Before
    public void setUp() throws Exception {
        this.environment = new MockEnvironment();
    }

    @Test
    public void testResolvePlaceholdersWithMap() {
        Map<String, Object> source = ofMap("A", "${A}", "B", ofArray("${B}"), "C", this);

        assertNull(resolvePlaceholders((Map) null, this.environment));
        assertNull(resolvePlaceholders((Map) null, null));

        assertSame(emptyMap(), resolvePlaceholders(emptyMap(), null));
        assertSame(emptyMap(), resolvePlaceholders(emptyMap(), this.environment));

        assertSame(source, resolvePlaceholders(source, null));

        assertNotSame(source, resolvePlaceholders(source, this.environment));

        Map<String, Object> resolvedSource = resolvePlaceholders(source, this.environment);
        assertEquals("${A}", resolvedSource.get("A"));
        assertArrayEquals(ofArray("${B}"), (String[]) resolvedSource.get("B"));
        assertSame(this, resolvedSource.get("C"));

        this.environment.setProperty("A", "1");
        this.environment.setProperty("B", "2");
        this.environment.setProperty("C", "3");

        resolvedSource = resolvePlaceholders(source, this.environment);
        assertEquals("1", resolvedSource.get("A"));
        assertArrayEquals(ofArray("2"), (String[]) resolvedSource.get("B"));
        assertSame(this, resolvedSource.get("C"));
    }

    @Test
    public void testResolvePlaceholdersWithArray() {
        String[] source = ofArray("${A}", "${B}", "${C}");

        assertNull(resolvePlaceholders((String[]) null, this.environment));
        assertNull(resolvePlaceholders((String[]) null, null));

        assertSame(EMPTY_STRING_ARRAY, resolvePlaceholders(EMPTY_STRING_ARRAY, this.environment));
        assertSame(EMPTY_STRING_ARRAY, resolvePlaceholders(EMPTY_STRING_ARRAY, null));

        assertSame(source, resolvePlaceholders(source, null));

        assertNotSame(source, resolvePlaceholders(source, this.environment));
        assertArrayEquals(source, resolvePlaceholders(source, this.environment));

        this.environment.setProperty("A", "1");
        this.environment.setProperty("B", "2");
        this.environment.setProperty("C", "3");
        assertArrayEquals(ofArray("1", "2", "3"), resolvePlaceholders(source, this.environment));
    }

    @Test
    public void testResolvePlaceholders() {
        String source = "${A} ${B} ${C}";

        assertNull(resolvePlaceholders((String) null, this.environment));

        assertSame(EMPTY_STRING, resolvePlaceholders(EMPTY_STRING, this.environment));
        assertSame(EMPTY_STRING, resolvePlaceholders(EMPTY_STRING, null));

        assertSame(source, resolvePlaceholders(source, null));

        assertNotSame(source, resolvePlaceholders(source, this.environment));
        assertEquals(source, resolvePlaceholders(source, this.environment));

        this.environment.setProperty("A", "1");
        this.environment.setProperty("B", "2");
        this.environment.setProperty("C", "3");
        assertEquals("1 2 3", resolvePlaceholders(source, this.environment));
    }
}