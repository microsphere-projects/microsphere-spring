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
package io.microsphere.spring.core;

import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static io.microsphere.spring.core.SpringVersion.CURRENT;
import static io.microsphere.spring.core.SpringVersion.SPRING_6_0;
import static io.microsphere.spring.core.SpringVersion.SPRING_6_1;
import static io.microsphere.spring.core.SpringVersion.SPRING_6_2;
import static io.microsphere.spring.core.SpringVersion.SPRING_7_0;
import static io.microsphere.spring.core.SpringVersion.SPRING_7_0_4;
import static io.microsphere.spring.core.SpringVersion.SPRING_7_0_5;
import static io.microsphere.spring.core.SpringVersion.resolveVersion;
import static io.microsphere.spring.core.SpringVersion.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SpringVersion} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class SpringVersionTest {

    @Test
    void testVersionRange() {
        // Spring Framework 6.0 -> [6.0.0, 6.0.23]
        testVersionRange(SPRING_6_0, 0, 23);
        // Spring Framework 6.1 -> [6.1.0, 6.0.16]
        testVersionRange(SPRING_6_1, 0, 21);
        // Spring Framework 6.2 -> [6.2.0, 6.2.16]
        testVersionRange(SPRING_6_2, 0, 16);
        // Spring Framework 7.0 -> [7.0.0, 7.0.5]
        testVersionRange(SPRING_7_0, 0, 5);
    }

    private void testVersionRange(SpringVersion baseVersion, int start, int end) {
        for (int i = start; i <= end; i++) {
            SpringVersion springVersion = valueOf(baseVersion.name() + "_" + i);
            Version version = springVersion.getVersion();
            assertNotNull(version);
            assertEquals(baseVersion.getMajor(), version.getMajor());
            assertEquals(baseVersion.getMinor(), version.getMinor());
            assertEquals(i, version.getPatch());
        }
    }

    @Test
    void testGetVersion() {
        for (SpringVersion springVersion : SpringVersion.values()) {
            if (CURRENT.equals(springVersion)) {
                continue;
            }
            Version version = resolveVersion(springVersion.name());
            assertEquals(springVersion.getVersion(), version);
            assertTrue(springVersion.getVersion().eq(version));
        }
    }

    @Test
    void testOperators() {
        assertEquals(7, SPRING_7_0_5.getMajor());
        assertEquals(0, SPRING_7_0_5.getMinor());
        assertEquals(5, SPRING_7_0_5.getPatch());

        assertTrue(SPRING_7_0_5.eq(SPRING_7_0_5));
        assertTrue(SPRING_7_0_5.equals(SPRING_7_0_5));
        assertTrue(SPRING_7_0_5.gt(SPRING_7_0_4));
        assertFalse(SPRING_7_0_4.isGreaterThan(SPRING_7_0_5));

        assertTrue(SPRING_7_0_5.ge(SPRING_7_0_5));
        assertTrue(SPRING_7_0_5.ge(SPRING_7_0_4));
        assertTrue(SPRING_7_0_5.isGreaterOrEqual(SPRING_7_0_5));
        assertFalse(SPRING_7_0_4.isGreaterOrEqual(SPRING_7_0_5));

        assertFalse(SPRING_7_0_5.lt(SPRING_7_0_4));
        assertTrue(SPRING_7_0_4.lt(SPRING_7_0_5));
        assertFalse(SPRING_7_0_5.isLessThan(SPRING_7_0_4));
        assertTrue(SPRING_7_0_4.isLessThan(SPRING_7_0_5));

        assertFalse(SPRING_7_0_5.le(SPRING_7_0_4));
        assertTrue(SPRING_7_0_5.le(SPRING_7_0_5));
        assertTrue(SPRING_7_0_5.isLessOrEqual(SPRING_7_0_5));
        assertTrue(SPRING_7_0_4.isLessOrEqual(SPRING_7_0_5));
    }
}