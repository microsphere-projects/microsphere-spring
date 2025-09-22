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

import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REFERENCE_KEY_REQUEST;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REFERENCE_KEY_RESPONSE;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REFERENCE_KEY_SESSION;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.REMOTE_USER_ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.context.request.ServerWebRequest.SESSION_MUTEX_ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.ATTRIBUTE_VALUE;
import static io.microsphere.spring.webflux.test.WebTestUtils.HEADER_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.HEADER_NAME_2;
import static io.microsphere.spring.webflux.test.WebTestUtils.HEADER_VALUE;
import static io.microsphere.spring.webflux.test.WebTestUtils.HEADER_VALUE_2;
import static io.microsphere.spring.webflux.test.WebTestUtils.NOT_FOUND_ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.PARAM_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.PARAM_NAME_2;
import static io.microsphere.spring.webflux.test.WebTestUtils.PARAM_VALUE;
import static io.microsphere.spring.webflux.test.WebTestUtils.PARAM_VALUE_2;
import static io.microsphere.spring.webflux.test.WebTestUtils.mockServerWebExchange;
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

    private MockServerWebExchange serverWebExchange;

    private ServerWebRequest serverWebRequest;

    @BeforeEach
    void setUp() {
        this.serverWebExchange = mockServerWebExchange();
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
        assertNotNull(this.serverWebRequest.getNativeRequest());
    }

    @Test
    void testGetNativeResponse() {
        assertNotNull(this.serverWebRequest.getNativeResponse());
    }

    @Test
    void testTestGetNativeRequest() {
        assertNotNull(this.serverWebRequest.getNativeRequest(ServerHttpRequest.class));
        assertNull(this.serverWebRequest.getNativeRequest(ServerWebRequestTest.class));
    }

    @Test
    void testTestGetNativeResponse() {
        assertNotNull(this.serverWebRequest.getNativeResponse(ServerHttpResponse.class));
        assertNull(this.serverWebRequest.getNativeResponse(ServerWebRequestTest.class));
    }

    @Test
    void testGetHeader() {
        assertSame(HEADER_VALUE, this.serverWebRequest.getHeader(HEADER_NAME));
    }

    @Test
    void testGetHeaderValues() {
        assertArrayEquals(HEADER_VALUE_2, this.serverWebRequest.getHeaderValues(HEADER_NAME_2));
    }

    @Test
    void testGetHeaderNames() {
        Iterator<String> iterator = this.serverWebRequest.getHeaderNames();
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
        assertEquals(PARAM_VALUE, this.serverWebRequest.getParameter(PARAM_NAME));
        assertEquals("test-param-value-2", this.serverWebRequest.getParameter(PARAM_NAME_2));
    }

    @Test
    void testGetParameterValues() {
        assertArrayEquals(new String[]{PARAM_VALUE}, this.serverWebRequest.getParameterValues(PARAM_NAME));
        assertArrayEquals(PARAM_VALUE_2, this.serverWebRequest.getParameterValues(PARAM_NAME_2));
    }

    @Test
    void testGetParameterNames() {
        Iterator<String> iterator = this.serverWebRequest.getParameterNames();
        assertTrue(iterator.hasNext());
        assertEquals(PARAM_NAME, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(PARAM_NAME_2, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testGetParameterMap() {
        Map<String, String[]> parameterMap = this.serverWebRequest.getParameterMap();
        assertEquals(2, parameterMap.size());
        assertArrayEquals(new String[]{PARAM_VALUE}, parameterMap.get(PARAM_NAME));
        assertArrayEquals(PARAM_VALUE_2, parameterMap.get(PARAM_NAME_2));
    }

    @Test
    void testGetLocale() {
        Locale locale = this.serverWebRequest.getLocale();
        assertEquals(getDefault(), locale);
    }

    @Test
    void testGetContextPath() {
        String contextPath = this.serverWebRequest.getContextPath();
        assertEquals("", contextPath);
    }

    @Test
    void testGetRemoteUser() {
        assertNull(this.serverWebRequest.getRemoteUser());
    }

    @Test
    void testGetUserPrincipal() {
        assertNull(this.serverWebRequest.getUserPrincipal());
    }

    @Test
    void testIsUserInRole() {
        assertFalse(this.serverWebRequest.isUserInRole(""));
    }

    @Test
    void testIsSecure() {
        assertFalse(this.serverWebRequest.isSecure());
    }

    @Test
    void testCheckNotModifiedWithLastModifiedTimestamp() {
        assertFalse(this.serverWebRequest.checkNotModified(0));
    }

    @Test
    void testCheckNotModifiedWithTag() {
        assertFalse(this.serverWebRequest.checkNotModified("null"));
    }

    @Test
    void testCheckNotModifiedWithTagAndLastModifiedTimestamp() {
        assertFalse(this.serverWebRequest.checkNotModified("null", 0));
    }

    @Test
    void testGetDescription() {
        String desc = this.serverWebRequest.getDescription(false);
        assertTrue(isNotBlank(desc));

        desc = this.serverWebRequest.getDescription(true);
        assertTrue(isNotBlank(desc));

        this.serverWebRequest.setAttribute(REMOTE_USER_ATTRIBUTE_NAME, "admin", SCOPE_REQUEST);

        desc = this.serverWebRequest.getDescription(true);
        assertTrue(isNotBlank(desc));
    }

    @Test
    void testGetAttribute() {
        assertEquals(ATTRIBUTE_VALUE, this.serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_REQUEST));
        assertEquals(ATTRIBUTE_VALUE, this.serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_SESSION));

        assertNull(this.serverWebRequest.getAttribute(NOT_FOUND_ATTRIBUTE_NAME, SCOPE_REQUEST));
        assertNull(this.serverWebRequest.getAttribute(NOT_FOUND_ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    @Test
    void testSetAttribute() {
        this.serverWebRequest.setAttribute(REMOTE_USER_ATTRIBUTE_NAME, "admin", SCOPE_REQUEST);
        assertEquals("admin", this.serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_REQUEST));

        this.serverWebRequest.setAttribute(REMOTE_USER_ATTRIBUTE_NAME, "admin", SCOPE_SESSION);
        assertEquals("admin", this.serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    @Test
    void testRemoveAttribute() {
        this.serverWebRequest.removeAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_REQUEST);
        assertNull(this.serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_REQUEST));

        this.serverWebRequest.removeAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_SESSION);
        assertNull(this.serverWebRequest.getAttribute(REMOTE_USER_ATTRIBUTE_NAME, SCOPE_SESSION));

        this.serverWebRequest.removeAttribute(ATTRIBUTE_NAME, SCOPE_REQUEST);
        assertNull(this.serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_REQUEST));

        this.serverWebRequest.removeAttribute(ATTRIBUTE_NAME, SCOPE_SESSION);
        assertNull(this.serverWebRequest.getAttribute(ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    @Test
    void testGetAttributeNames() {
        assertNotNull(this.serverWebRequest.getAttributeNames(SCOPE_REQUEST));
        assertNotNull(this.serverWebRequest.getAttributeNames(SCOPE_SESSION));
    }

    @Test
    void testRegisterDestructionCallback() {
        this.serverWebRequest.registerDestructionCallback("test", () -> {
        }, SCOPE_REQUEST);
    }

    @Test
    void testResolveReference() {
        assertSame(this.serverWebRequest.getNativeRequest(), this.serverWebRequest.resolveReference(REFERENCE_KEY_REQUEST));
        assertSame(this.serverWebRequest.getNativeResponse(), this.serverWebRequest.resolveReference(REFERENCE_KEY_RESPONSE));
        assertSame(this.serverWebRequest.getSession(), this.serverWebRequest.resolveReference(REFERENCE_KEY_SESSION));
        assertNull(this.serverWebRequest.resolveReference("others"));
    }

    @Test
    void testGetSessionId() {
        assertNotNull(this.serverWebRequest.getSessionId());
    }

    @Test
    void testGetSessionMutex() {
        assertSame(this.serverWebRequest.getSession(), this.serverWebRequest.getSessionMutex());

        this.serverWebRequest.setAttribute(SESSION_MUTEX_ATTRIBUTE_NAME, this, SCOPE_SESSION);
        assertSame(this, this.serverWebRequest.getSessionMutex());
    }

    @Test
    void testGetExchange() {
        assertSame(this.serverWebExchange, this.serverWebRequest.getExchange());
    }

    @Test
    void testGetRequest() {
        assertSame(this.serverWebRequest.getNativeRequest(), this.serverWebRequest.getRequest());
        assertSame(this.serverWebExchange.getRequest(), this.serverWebRequest.getRequest());
    }

    @Test
    void testGetResponse() {
        assertSame(this.serverWebRequest.getNativeResponse(), this.serverWebRequest.getResponse());
        assertSame(this.serverWebExchange.getResponse(), this.serverWebRequest.getResponse());
    }

    @Test
    void testToArray() {
        assertNull(this.serverWebRequest.toArray(null));
    }

}