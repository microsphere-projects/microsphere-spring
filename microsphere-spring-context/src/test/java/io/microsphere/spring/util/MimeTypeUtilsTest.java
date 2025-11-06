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
package io.microsphere.spring.util;

import io.microsphere.spring.util.MimeTypeUtils.SpecificityComparator;
import org.junit.jupiter.api.Test;
import org.springframework.util.MimeType;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.util.MimeTypeUtils.APPLICATION_GRAPHQL;
import static io.microsphere.spring.util.MimeTypeUtils.APPLICATION_GRAPHQL_VALUE;
import static io.microsphere.spring.util.MimeTypeUtils.equalsTypeAndSubtype;
import static io.microsphere.spring.util.MimeTypeUtils.getSubtypeSuffix;
import static io.microsphere.spring.util.MimeTypeUtils.isPresentIn;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.MimeType.valueOf;

/**
 * {@link MimeTypeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MimeTypeUtils
 * @since 1.0.0
 */
class MimeTypeUtilsTest {

    private static final MimeType APPLICATION_TEXT = new MimeType("application", "text");

    @Test
    void testConstants() {
        assertEquals("application", APPLICATION_GRAPHQL.getType());
        assertEquals("graphql+json", APPLICATION_GRAPHQL.getSubtype());
        assertEquals("application/graphql+json", APPLICATION_GRAPHQL_VALUE);
    }

    @Test
    void testEqualsTypeAndSubtype() {
        assertTrue(equalsTypeAndSubtype(APPLICATION_GRAPHQL, APPLICATION_GRAPHQL));
        assertTrue(equalsTypeAndSubtype(APPLICATION_GRAPHQL, new MimeType("application", "graphql+json")));
        assertTrue(equalsTypeAndSubtype(new MimeType("application", "graphql+json"), APPLICATION_GRAPHQL));
        assertFalse(equalsTypeAndSubtype(null, APPLICATION_GRAPHQL));
        assertFalse(equalsTypeAndSubtype(APPLICATION_GRAPHQL, null));
        assertFalse(equalsTypeAndSubtype(APPLICATION_GRAPHQL, APPLICATION_TEXT));
        assertFalse(equalsTypeAndSubtype(APPLICATION_TEXT, APPLICATION_GRAPHQL));
    }

    @Test
    void testGetSubtypeSuffix() {
        assertNull(getSubtypeSuffix(null));
        assertNull(getSubtypeSuffix(new MimeType("application")));
        assertNull(getSubtypeSuffix(new MimeType("application", "json")));
        assertEquals("json", getSubtypeSuffix(APPLICATION_GRAPHQL));
        assertEquals("xml", getSubtypeSuffix(new MimeType("application", "text+xml")));
    }

    @Test
    void testIsPresentIn() {
        assertFalse(isPresentIn(APPLICATION_GRAPHQL, null));
        assertFalse(isPresentIn(APPLICATION_GRAPHQL, emptyList()));
        assertFalse(isPresentIn(APPLICATION_GRAPHQL, ofList(APPLICATION_TEXT)));
        assertTrue(isPresentIn(APPLICATION_GRAPHQL, asList(APPLICATION_TEXT, APPLICATION_GRAPHQL)));
        assertTrue(isPresentIn(APPLICATION_GRAPHQL, ofList(APPLICATION_GRAPHQL)));
    }

    @Test
    void testSpecificityComparator() {
        SpecificityComparator<MimeType> comparator = new SpecificityComparator<>();

        MimeType allTypes = valueOf("*/*");
        MimeType audioType = valueOf("audio/*");
        assertEquals(1, comparator.compare(allTypes, audioType));
        assertEquals(0, comparator.compare(allTypes, allTypes));
        assertEquals(-1, comparator.compare(audioType, allTypes));
        assertEquals(0, comparator.compare(audioType, audioType));

        MimeType audioWildcard = valueOf("audio/*");
        MimeType audioBasic = valueOf("audio/basic");
        MimeType audioWave = valueOf("audio/wave");
        assertEquals(1, comparator.compare(audioWildcard, audioBasic));
        assertEquals(-1, comparator.compare(audioBasic, audioWildcard));
        assertEquals(0, comparator.compare(audioBasic, audioWave));

        MimeType withParams = valueOf("audio/basic;level=1;charset=utf-8");
        MimeType withoutParams = valueOf("audio/basic");
        assertEquals(-1, comparator.compare(withParams, withoutParams));
        assertEquals(1, comparator.compare(withoutParams, withParams));

        audioType = valueOf("audio/basic");
        MimeType textType = valueOf("text/html");
        assertEquals(0, comparator.compare(audioType, textType));
        assertEquals(0, comparator.compare(textType, audioType));

        MimeType withOneParam = MimeType.valueOf("application/json;version=1");
        MimeType withTwoParams = MimeType.valueOf("application/json;version=1;charset=utf-8");
        assertEquals(1, comparator.compare(withOneParam, withTwoParams));
        assertEquals(-1, comparator.compare(withTwoParams, withOneParam));
    }
}
