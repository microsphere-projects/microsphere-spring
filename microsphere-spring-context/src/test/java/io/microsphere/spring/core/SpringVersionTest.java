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
import org.junit.Test;

import static io.microsphere.spring.core.SpringVersion.CURRENT;
import static io.microsphere.spring.core.SpringVersion.SPRING_5_3_38;
import static io.microsphere.spring.core.SpringVersion.SPRING_5_3_39;
import static io.microsphere.spring.core.SpringVersion.resolveVersion;
import static io.microsphere.spring.core.SpringVersion.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link SpringVersion} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringVersionTest {

    @Test
    public void testVersionRange() {
        // Spring Framework 3.2 -> [3.2.0, 3.2.18]
        testVersionRange(SpringVersion.SPRING_3_2, 0, 18);
        // Spring Framework 4.0 -> [4.0.0, 4.0.9]
        testVersionRange(SpringVersion.SPRING_4_0, 0, 9);
        // Spring Framework 4.1 -> [4.1.0, 4.1.9]
        testVersionRange(SpringVersion.SPRING_4_1, 0, 9);
        // Spring Framework 4.2 -> [4.2.0, 4.2.9]
        testVersionRange(SpringVersion.SPRING_4_2, 0, 9);
        // Spring Framework 4.3 -> [4.3.0, 4.3.30]
        testVersionRange(SpringVersion.SPRING_4_3, 0, 30);
        // Spring Framework 5.0 -> [5.0.0, 5.0.20]
        testVersionRange(SpringVersion.SPRING_5_0, 0, 20);
        // Spring Framework 5.1 -> [5.1.0, 5.1.19]
        testVersionRange(SpringVersion.SPRING_5_1, 0, 20);
        // Spring Framework 5.2 -> [5.2.0, 5.2.25]
        testVersionRange(SpringVersion.SPRING_5_2, 0, 25);
        // Spring Framework 5.3 -> [5.3.0, 5.3.39]
        testVersionRange(SpringVersion.SPRING_5_3, 0, 39);

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
    public void testGetVersion() {
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
    public void testOperators() {
        assertEquals(5, SPRING_5_3_39.getMajor());
        assertEquals(3, SPRING_5_3_39.getMinor());
        assertEquals(39, SPRING_5_3_39.getPatch());

        assertTrue(SPRING_5_3_39.eq(SPRING_5_3_39));
        assertTrue(SPRING_5_3_39.equals(SPRING_5_3_39));
        assertTrue(SPRING_5_3_39.gt(SPRING_5_3_38));
        assertFalse(SPRING_5_3_38.isGreaterThan(SPRING_5_3_39));

        assertTrue(SPRING_5_3_39.ge(SPRING_5_3_39));
        assertTrue(SPRING_5_3_39.ge(SPRING_5_3_38));
        assertTrue(SPRING_5_3_39.isGreaterOrEqual(SPRING_5_3_39));
        assertFalse(SPRING_5_3_38.isGreaterOrEqual(SPRING_5_3_39));

        assertFalse(SPRING_5_3_39.lt(SPRING_5_3_38));
        assertTrue(SPRING_5_3_38.lt(SPRING_5_3_39));
        assertFalse(SPRING_5_3_39.isLessThan(SPRING_5_3_38));
        assertTrue(SPRING_5_3_38.isLessThan(SPRING_5_3_39));

        assertFalse(SPRING_5_3_39.le(SPRING_5_3_38));
        assertTrue(SPRING_5_3_39.le(SPRING_5_3_39));
        assertTrue(SPRING_5_3_39.isLessOrEqual(SPRING_5_3_39));
        assertTrue(SPRING_5_3_38.isLessOrEqual(SPRING_5_3_39));
    }
}