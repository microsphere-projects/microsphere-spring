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

import org.junit.Test;
import org.springframework.util.MimeType;

import static io.microsphere.spring.util.MimeTypeUtils.APPLICATION_GRAPHQL;
import static io.microsphere.spring.util.MimeTypeUtils.APPLICATION_GRAPHQL_VALUE;
import static io.microsphere.spring.util.MimeTypeUtils.equalsTypeAndSubtype;
import static io.microsphere.spring.util.MimeTypeUtils.getSubtypeSuffix;
import static io.microsphere.spring.util.MimeTypeUtils.isPresentIn;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link MimeTypeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MimeTypeUtils
 * @since 1.0.0
 */
public class MimeTypeUtilsTest {

    private static final MimeType APPLICATION_TEXT = new MimeType("application", "text");

    @Test
    public void testConstants() {
        assertEquals("application", APPLICATION_GRAPHQL.getType());
        assertEquals("graphql+json", APPLICATION_GRAPHQL.getSubtype());
        assertEquals("application/graphql+json", APPLICATION_GRAPHQL_VALUE);
    }

    @Test
    public void testEqualsTypeAndSubtype() {
        assertTrue(equalsTypeAndSubtype(APPLICATION_GRAPHQL, APPLICATION_GRAPHQL));
        assertTrue(equalsTypeAndSubtype(APPLICATION_GRAPHQL, new MimeType("application", "graphql+json")));
        assertTrue(equalsTypeAndSubtype(new MimeType("application", "graphql+json"), APPLICATION_GRAPHQL));
        assertFalse(equalsTypeAndSubtype(null, APPLICATION_GRAPHQL));
        assertFalse(equalsTypeAndSubtype(APPLICATION_GRAPHQL, null));
        assertFalse(equalsTypeAndSubtype(APPLICATION_GRAPHQL, APPLICATION_TEXT));
        assertFalse(equalsTypeAndSubtype(APPLICATION_TEXT, APPLICATION_GRAPHQL));
    }

    @Test
    public void testGetSubtypeSuffix() {
        assertNull(getSubtypeSuffix(null));
        assertNull(getSubtypeSuffix(new MimeType("application")));
        assertNull(getSubtypeSuffix(new MimeType("application", "json")));
        assertEquals("json", getSubtypeSuffix(APPLICATION_GRAPHQL));
        assertEquals("xml", getSubtypeSuffix(new MimeType("application", "text+xml")));
    }

    @Test
    public void testIsPresentIn() {
        assertFalse(isPresentIn(APPLICATION_GRAPHQL, null));
        assertFalse(isPresentIn(APPLICATION_GRAPHQL, emptyList()));
        assertFalse(isPresentIn(APPLICATION_GRAPHQL, asList(APPLICATION_TEXT)));
        assertTrue(isPresentIn(APPLICATION_GRAPHQL, asList(APPLICATION_TEXT, APPLICATION_GRAPHQL)));
        assertTrue(isPresentIn(APPLICATION_GRAPHQL, asList(APPLICATION_GRAPHQL)));
    }
}
