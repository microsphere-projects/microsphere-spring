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

package io.microsphere.spring.webflux.context.request;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static io.microsphere.spring.webflux.util.AttributeScopeTest.HEADER_NAME;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.HEADER_NAME_2;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.HEADER_VALUE;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.HEADER_VALUE_2;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.PARAM_NAME;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.PARAM_NAME_2;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.PARAM_VALUE;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.PARAM_VALUE_2;
import static io.microsphere.spring.webflux.util.AttributeScopeTest.mockServerWebExchange;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NativeWebRequestAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NativeWebRequestAdapter
 * @since 1.0.0
 */
class NativeWebRequestAdapterTest {

    private NativeWebRequestAdapter nativeWebRequest;

    @BeforeEach
    void setUp() {
        MockServerWebExchange serverWebExchange = mockServerWebExchange();
        this.nativeWebRequest = new NativeWebRequestAdapter(serverWebExchange);
    }

    @Test
    void testGetNativeRequest() {
        assertNotNull(nativeWebRequest.getNativeRequest());
    }

    @Test
    void testGetNativeResponse() {
        assertNotNull(nativeWebRequest.getNativeResponse());
    }

    @Test
    void testTestGetNativeRequest() {
        assertNotNull(nativeWebRequest.getNativeRequest(ServerHttpRequest.class));
        assertNull(nativeWebRequest.getNativeRequest(NativeWebRequestAdapterTest.class));
    }

    @Test
    void testTestGetNativeResponse() {
        assertNotNull(nativeWebRequest.getNativeResponse(ServerHttpResponse.class));
        assertNull(nativeWebRequest.getNativeResponse(NativeWebRequestAdapterTest.class));
    }

    @Test
    void testGetHeader() {
        assertSame(HEADER_VALUE, nativeWebRequest.getHeader(HEADER_NAME));
    }

    @Test
    void testGetHeaderValues() {
        assertArrayEquals(HEADER_VALUE_2, nativeWebRequest.getHeaderValues(HEADER_NAME_2));
    }

    @Test
    void testGetHeaderNames() {
        Iterator<String> iterator = nativeWebRequest.getHeaderNames();
        assertTrue(iterator.hasNext());
        assertSame(HEADER_NAME, iterator.next());
        assertTrue(iterator.hasNext());
        assertSame(HEADER_NAME_2, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testGetParameter() {
        assertEquals(PARAM_VALUE, nativeWebRequest.getParameter(PARAM_NAME));
        assertEquals("test-param-value-2", nativeWebRequest.getParameter(PARAM_NAME_2));
    }

    @Test
    void testGetParameterValues() {
        assertArrayEquals(new String[]{PARAM_VALUE}, nativeWebRequest.getParameterValues(PARAM_NAME));
        assertArrayEquals(PARAM_VALUE_2, nativeWebRequest.getParameterValues(PARAM_NAME_2));
    }

    @Test
    void testGetParameterNames() {
        Iterator<String> iterator = nativeWebRequest.getParameterNames();
        assertTrue(iterator.hasNext());
        assertEquals(PARAM_NAME, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(PARAM_NAME_2, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testGetParameterMap() {
        Map<String, String[]> parameterMap = nativeWebRequest.getParameterMap();
        assertEquals(2, parameterMap.size());
        assertArrayEquals(new String[]{PARAM_VALUE}, parameterMap.get(PARAM_NAME));
        assertArrayEquals(PARAM_VALUE_2, parameterMap.get(PARAM_NAME_2));
    }

    @Test
    void testGetLocale() {
        Locale locale = nativeWebRequest.getLocale();
        assertNull(locale);
     }

    @Test
    void testGetContextPath() {
    }

    @Test
    void testGetRemoteUser() {
    }

    @Test
    void testGetUserPrincipal() {
    }

    @Test
    void testIsUserInRole() {
    }

    @Test
    void testIsSecure() {
    }

    @Test
    void testCheckNotModified() {
    }

    @Test
    void testTestCheckNotModified() {
    }

    @Test
    void testTestCheckNotModified1() {
    }

    @Test
    void testGetDescription() {
    }

    @Test
    void testGetAttribute() {
    }

    @Test
    void testSetAttribute() {
    }

    @Test
    void testRemoveAttribute() {
    }

    @Test
    void testGetAttributeNames() {
    }

    @Test
    void testRegisterDestructionCallback() {
    }

    @Test
    void testResolveReference() {
    }

    @Test
    void testGetSessionId() {
    }

    @Test
    void testGetSessionMutex() {
    }
}