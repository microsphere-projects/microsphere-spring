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

package io.microsphere.spring.web.util;


import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMethod;

import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;
import static io.microsphere.spring.web.util.HttpUtils.supportsMethod;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.values;

/**
 * {@link HttpUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HttpUtils
 * @since 1.0.0
 */
public class HttpUtilsTest {

    @Test
    public void testALL_HTTP_METHODS() {
        assertTrue(ALL_HTTP_METHODS.contains("GET"));
        assertTrue(ALL_HTTP_METHODS.contains("HEAD"));
        assertTrue(ALL_HTTP_METHODS.contains("POST"));
        assertTrue(ALL_HTTP_METHODS.contains("PUT"));
        assertTrue(ALL_HTTP_METHODS.contains("PATCH"));
        assertTrue(ALL_HTTP_METHODS.contains("DELETE"));
        assertTrue(ALL_HTTP_METHODS.contains("OPTIONS"));
        assertTrue(ALL_HTTP_METHODS.contains("TRACE"));
        assertFalse(ALL_HTTP_METHODS.contains("OTHER"));
    }

    @Test
    public void testALL_HTTP_METHODSOnUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> ALL_HTTP_METHODS.add("AAA"));
    }

    @Test
    public void testSupportsMethod() {
        assertTrue(supportsMethod("GET"));
        assertTrue(supportsMethod("HEAD"));
        assertTrue(supportsMethod("POST"));
        assertTrue(supportsMethod("PUT"));
        assertTrue(supportsMethod("PATCH"));
        assertTrue(supportsMethod("DELETE"));
        assertTrue(supportsMethod("OPTIONS"));
        assertTrue(supportsMethod("TRACE"));
        assertFalse(supportsMethod("OTHER"));
    }

    @Test
    public void testSupportsMethodWithHttpMethod() {
        for (HttpMethod method : values()) {
            assertTrue(supportsMethod(method));
        }
    }

    @Test
    public void testSupportsMethodWithNullHttpMethod() {
        assertFalse(supportsMethod((HttpMethod) null));
    }

    @Test
    public void testSupportsMethodWithRequestMethod() {
        for (RequestMethod method : RequestMethod.values()) {
            assertTrue(supportsMethod(method));
        }
    }

    @Test
    public void testSupportsMethodWithNullRequestMethod() {
        assertFalse(supportsMethod((RequestMethod) null));
    }
}