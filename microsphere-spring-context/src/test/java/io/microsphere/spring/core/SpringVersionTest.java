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

import static io.microsphere.spring.core.SpringVersion.resolveVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        testVersionRange(SpringVersion.SPRING_6_0, 0, 23);
        // Spring Framework 6.1 -> [6.1.0, 6.0.16]
        testVersionRange(SpringVersion.SPRING_6_1, 0, 21);
        // Spring Framework 6.2 -> [6.2.0, 6.2.1]
        testVersionRange(SpringVersion.SPRING_6_2, 0, 9);
    }

    private void testVersionRange(SpringVersion baseVersion, int start, int end) {
        for (int i = start; i <= end; i++) {
            SpringVersion springVersion = SpringVersion.valueOf(baseVersion.name() + "_" + i);
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
            if (SpringVersion.CURRENT.equals(springVersion)) {
                continue;
            }
            Version version = resolveVersion(springVersion.name());
            assertEquals(springVersion.getVersion(), version);
            assertTrue(springVersion.getVersion().eq(version));
        }
    }
}
