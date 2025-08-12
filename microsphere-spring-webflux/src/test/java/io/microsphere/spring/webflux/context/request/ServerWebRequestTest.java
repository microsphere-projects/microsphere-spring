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

import static io.microsphere.spring.webflux.WebTestUtils.ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.WebTestUtils.ATTRIBUTE_VALUE;
import static io.microsphere.spring.webflux.WebTestUtils.HEADER_NAME;
import static io.microsphere.spring.webflux.WebTestUtils.HEADER_NAME_2;
import static io.microsphere.spring.webflux.WebTestUtils.HEADER_VALUE;
import static io.microsphere.spring.webflux.WebTestUtils.HEADER_VALUE_2;
import static io.microsphere.spring.webflux.WebTestUtils.NOT_FOUND_ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.WebTestUtils.PARAM_NAME;
import static io.microsphere.spring.webflux.WebTestUtils.PARAM_NAME_2;
import static io.microsphere.spring.webflux.WebTestUtils.PARAM_VALUE;
import static io.microsphere.spring.webflux.WebTestUtils.PARAM_VALUE_2;
import static io.microsphere.spring.webflux.WebTestUtils.mockServerWebExchange;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REFERENCE_KEY_REQUEST;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REFERENCE_KEY_RESPONSE;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REFERENCE_KEY_SESSION;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REMOTE_USER_ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.SESSION_MUTEX_ATTRIBUTE_NAME;
import static java.util.Locale.getDefault;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * {@link ServerWebRequest} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServerWebRequest
 * @since 1.0.0
 */
class ServerWebRequestTest {

    private ServerWebRequest serverWebRequest;

    @BeforeEach
    void setUp() {
        MockServerWebExchange serverWebExchange = mockServerWebExchange();
        this.serverWebRequest = new ServerWebRequest(serverWebExchange);
    }

    @Test
    void testConstants() {
        assertEquals("REMOTE_USER", REMOTE_USER_ATTRIBUTE_NAME);
        assertEquals("request", REFERENCE_KEY_REQUEST);
        assertEquals("response", REFERENCE_KEY_RESPONSE);
        assertEquals("session", REFERENCE_KEY_SESSION);
        assertEquals("org.springframework.web.util.WebUtils.MUTEX", SESSION_MUTEX_ATTRIBUTE_NAME);
    }

    @Test
    void testGetNativeRequest() {
        assertNotNull(serverWebRequest.getNativeRequest());
    }

    @Test
    void testGetNativeResponse() {
        assertNotNull(serverWebRequest.getNativeResponse());
    }

    @Test
    void testTestGetNativeRequest() {
        assertNotNull(serverWebRequest.getNativeRequest(ServerHttpRequest.class));
        assertNull(serverWebRequest.getNativeRequest(ServerWebRequestTest.class));
    }

    @Test
    void testTestGetNativeResponse() {
        assertNotNull(serverWebRequest.getNativeResponse(ServerHttpResponse.class));
        assertNull(serverWebRequest.getNativeResponse(ServerWebRequestTest.class));
    }

    @Test
    void testGetHeader() {
        assertSame(HEADER_VALUE, serverWebRequest.getHeader(HEADER_NAME));
    }

    @Test
    void testGetHeaderValues() {
        assertArrayEquals(HEADER_VALUE_2, serverWebRequest.getHeaderValues(HEADER_NAME_2));
    }

    @Test
    void testGetHeaderNames() {
        Iterator<String> iterator = serverWebRequest.getHeaderNames();
        assertTrue(iterator.hasNext());
        assertSame(HEADER_NAME, iterator.next());
        assertTrue(iterator.hasNext());
        assertSame(HEADER_NAME_2, iterator.next());
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testGetParameter() {
        assertEquals(PARAM_VALUE, serverWebRequest.getParameter(PARAM_NAME));
        assertEquals("test-param-value-2", serverWebRequest.getParameter(PARAM_NAME_2));
    }

    @Test
    void testGetParameterValues() {
        assertArrayEquals(new String[]{PARAM_VALUE}, serverWebRequest.getParameterValues(PARAM_NAME));
        assertArrayEquals(PARAM_VALUE_2, serverWebRequest.getParameterValues(PARAM_NAME_2));
    }

    @Test
    void testGetParameterNames() {
        Iterator<String> iterator = serverWebRequest.getParameterNames();
        assertTrue(iterator.hasNext());
        assertEquals(PARAM_NAME, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(PARAM_NAME_2, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testGetParameterMap() {
        Map<String, String[]> parameterMap = serverWebRequest.getParameterMap();
        assertEquals(2, parameterMap.size());
        assertArrayEquals(new String[]{PARAM_VALUE}, parameterMap.get(PARAM_NAME));
        assertArrayEquals(PARAM_VALUE_2, parameterMap.get(PARAM_NAME_2));
    }

    @Test
    void testGetLocale() {
        Locale locale = serverWebRequest.getLocale();
        assertEquals(getDefault(), locale);
    }

    @Test
    void testGetContextPath() {
        String contextPath = serverWebRequest.getContextPath();
        assertEquals("", contextPath);
    }

    @Test
    void testGetRemoteUser() {
        assertNull(serverWebRequest.getRemoteUser());
    }

    @Test
    void testGetUserPrincipal() {
        assertNull(serverWebRequest.getUserPrincipal());
    }

    @Test
    void testIsUserInRole() {
        assertFalse(serverWebRequest.isUserInRole(""));
    }

    @Test
    void testIsSecure() {
        assertFalse(serverWebRequest.isSecure());
    }

    @Test
    void testCheckNotModifiedWithLastModifiedTimestamp() {
        assertFalse(serverWebRequest.checkNotModified(0));
    }

    @Test
    void testCheckNotModifiedWithTag() {
        assertFalse(serverWebRequest.checkNotModified("null"));
    }

    @Test
    void testCheckNotModifiedWithTagAndLastModifiedTimestamp() {
        assertFalse(serverWebRequest.checkNotModified("null", 0));
    }

    @Test
    void testGetDescription() {
        String desc = serverWebRequest.getDescription(false);
        assertTrue(isNotBlank(desc));

        desc = serverWebRequest.getDescription(true);
        assertTrue(isNotBlank(desc));

        serverWebRequest.setAttribute(REMOTE_USER_ATTRIBUTE_NAME, "admin", SCOPE_REQUEST);

        desc = serverWebRequest.getDescription(true);
        assertTrue(isNotBlank(desc));
    }

    @Test
    void testGetAttribute() {
        assertEquals(ATTRIBUTE_VALUE, serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_REQUEST));
        assertEquals(ATTRIBUTE_VALUE, serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_SESSION));

        assertNull(serverWebRequest.getAttribute(NOT_FOUND_ATTRIBUTE_NAME, SCOPE_REQUEST));
        assertNull(serverWebRequest.getAttribute(NOT_FOUND_ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    @Test
    void testSetAttribute() {
        serverWebRequest.setAttribute(REMOTE_USER_ATTRIBUTE_NAME, "admin", SCOPE_REQUEST);
        assertEquals("admin", serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_REQUEST));

        serverWebRequest.setAttribute(REMOTE_USER_ATTRIBUTE_NAME, "admin", SCOPE_SESSION);
        assertEquals("admin", serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    @Test
    void testRemoveAttribute() {
        serverWebRequest.removeAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_REQUEST);
        assertNull(serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_REQUEST));

        serverWebRequest.removeAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_SESSION);
        assertNull(serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_SESSION));

        serverWebRequest.removeAttribute(ATTRIBUTE_NAME, SCOPE_REQUEST);
        assertNull(serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_REQUEST));

        serverWebRequest.removeAttribute(ATTRIBUTE_NAME, SCOPE_SESSION);
        assertNull(serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    @Test
    void testGetAttributeNames() {
        assertNotNull(serverWebRequest.getAttributeNames(SCOPE_REQUEST));
        assertNotNull(serverWebRequest.getAttributeNames(SCOPE_SESSION));
    }

    @Test
    void testRegisterDestructionCallback() {
        serverWebRequest.registerDestructionCallback("test", () -> {
        }, SCOPE_REQUEST);
    }

    @Test
    void testResolveReference() {
        assertSame(serverWebRequest.getNativeRequest(), serverWebRequest.resolveReference(REFERENCE_KEY_REQUEST));
        assertSame(serverWebRequest.getNativeResponse(), serverWebRequest.resolveReference(REFERENCE_KEY_RESPONSE));
        assertSame(serverWebRequest.getSession(), serverWebRequest.resolveReference(REFERENCE_KEY_SESSION));
        assertNull(serverWebRequest.resolveReference("others"));
    }

    @Test
    void testGetSessionId() {
        assertNotNull(serverWebRequest.getSessionId());
    }

    @Test
    void testGetSessionMutex() {
        assertSame(serverWebRequest.getSession(), serverWebRequest.getSessionMutex());

        serverWebRequest.setAttribute(SESSION_MUTEX_ATTRIBUTE_NAME, this, SCOPE_SESSION);
        assertSame(this, serverWebRequest.getSessionMutex());
    }

    @Test
    void testToArray() {
        assertNull(serverWebRequest.toArray(null));
    }

}