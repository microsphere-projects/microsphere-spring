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


import io.microsphere.spring.web.AbstractSpringWebTest;
import org.junit.Test;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.web.util.WebRequestUtils.PATH_ATTRIBUTE;
import static io.microsphere.spring.web.util.WebRequestUtils.getContentType;
import static io.microsphere.spring.web.util.WebRequestUtils.getMethod;
import static io.microsphere.spring.web.util.WebRequestUtils.getResolvedLookupPath;
import static io.microsphere.spring.web.util.WebRequestUtils.hasBody;
import static io.microsphere.spring.web.util.WebRequestUtils.isPreFlightRequest;
import static io.microsphere.spring.web.util.WebRequestUtils.parseContentType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * {@link WebRequestUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestUtils
 * @since 1.0.0
 */
public class WebRequestUtilsTest extends AbstractSpringWebTest {

    @Test
    public void testGetMethod() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(":METHOD:", "POST"));
        assertEquals("POST", getMethod(request));

        request = createWebRequest(r -> r.setMethod("POST"));
        assertEquals("POST", getMethod(request));
    }

    @Test
    public void testIsPreFlightRequest() {
        NativeWebRequest request = createPreFightRequest();
        assertTrue(isPreFlightRequest(request));
        request = createWebRequest();
        assertFalse(isPreFlightRequest(request));
    }

    @Test
    public void testGetContentType() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(CONTENT_TYPE, "application/json"));
        assertEquals("application/json", getContentType(request));
    }

    @Test
    public void testParseContentType() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(CONTENT_TYPE, "application/json"));
        assertEquals(APPLICATION_JSON, parseContentType(request));
    }

    @Test
    public void testHasBody() {
        NativeWebRequest request = createWebRequest();
        assertFalse(hasBody(request));

        request = createWebRequest(r -> r.addHeader(CONTENT_LENGTH, "1"));
        assertTrue(hasBody(request));

        request = createWebRequest(r -> r.addHeader(CONTENT_LENGTH, "0"));
        assertFalse(hasBody(request));

        request = createWebRequest(r -> r.addHeader(TRANSFER_ENCODING, "1"));
        assertTrue(hasBody(request));
    }

    @Test
    public void testGetResolvedLookupPath() {
        NativeWebRequest request = createWebRequest(r -> r.setAttribute(PATH_ATTRIBUTE, "/"));
        assertEquals("/", getResolvedLookupPath(request));
    }
}