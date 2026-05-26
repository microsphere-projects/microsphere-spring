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
package io.microsphere.spring.config.env.support;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultResourceComparator} Test
 *
 * @see DefaultResourceComparator
 * @since 1.0.0
 */
class DefaultResourceComparatorTest {

    private final DefaultResourceComparator comparator = DefaultResourceComparator.INSTANCE;

    @Test
    void testInstanceIsSingleton() {
        assertSame(DefaultResourceComparator.INSTANCE, comparator);
    }

    @Test
    void testCompareWithDifferentFilenames() {
        FileSystemResource r1 = new FileSystemResource("/tmp/a.txt");
        FileSystemResource r2 = new FileSystemResource("/tmp/b.txt");
        assertTrue(comparator.compare(r1, r2) < 0, "a.txt should come before b.txt");
        assertTrue(comparator.compare(r2, r1) > 0, "b.txt should come after a.txt");
    }

    @Test
    void testCompareWithEqualFilenames() {
        FileSystemResource r1 = new FileSystemResource("/some/path/file.txt");
        FileSystemResource r2 = new FileSystemResource("/other/path/file.txt");
        assertEquals(0, comparator.compare(r1, r2), "Same filename should compare as equal");
    }

    @Test
    void testCompareOrdering() {
        FileSystemResource r1 = new FileSystemResource("/tmp/1.json");
        FileSystemResource r2 = new FileSystemResource("/tmp/2.json");
        assertTrue(comparator.compare(r1, r2) < 0);
        assertTrue(comparator.compare(r2, r1) > 0);
        assertEquals(0, comparator.compare(r1, r1));
    }
}
