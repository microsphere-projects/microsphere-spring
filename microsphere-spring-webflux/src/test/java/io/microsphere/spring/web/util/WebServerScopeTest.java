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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockServerWebExchange;

import static io.microsphere.spring.web.util.WebServerScope.REQUEST;
import static io.microsphere.spring.web.util.WebServerScope.SESSION;
import static io.microsphere.spring.web.util.WebServerScope.getAttribute;
import static io.microsphere.spring.web.util.WebServerScope.getAttributeNames;
import static io.microsphere.spring.web.util.WebServerScope.removeAttribute;
import static io.microsphere.spring.web.util.WebServerScope.setAttribute;
import static io.microsphere.spring.web.util.WebServerScope.valueOf;
import static io.microsphere.spring.webflux.test.WebTestUtils.ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.ATTRIBUTE_VALUE;
import static io.microsphere.spring.webflux.test.WebTestUtils.NOT_FOUND_ATTRIBUTE_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.mockServerWebExchange;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * {@link WebServerScope} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebServerScope
 * @since 1.0.0
 */
class WebServerScopeTest {

    private MockServerWebExchange serverWebExchange;

    @BeforeEach
    void setUp() {
        this.serverWebExchange = mockServerWebExchange();
    }

    @Test
    void testValue() {
        assertEquals(SCOPE_REQUEST, REQUEST.value());
        assertEquals(SCOPE_SESSION, SESSION.value());
    }

    @Test
    void testGetAttribute() {
        assertEquals(ATTRIBUTE_VALUE, REQUEST.getAttribute(this.serverWebExchange, ATTRIBUTE_NAME));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getAttribute(this.serverWebExchange, ATTRIBUTE_NAME));

        assertNull(REQUEST.getAttribute(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME));
        assertNull(SESSION.getAttribute(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME));
    }

    @Test
    void testGetAttributeWithNullName() {
        assertNull(REQUEST.setAttribute(this.serverWebExchange, null, ATTRIBUTE_VALUE));
        assertNull(SESSION.setAttribute(this.serverWebExchange, null, ATTRIBUTE_VALUE));
    }

    @Test
    void testGetAttributeWithNullValue() {
        assertNull(REQUEST.setAttribute(this.serverWebExchange, ATTRIBUTE_NAME, null));
        assertNull(SESSION.setAttribute(this.serverWebExchange, ATTRIBUTE_NAME, null));
    }

    @Test
    void testGetRequiredAttribute() {
        assertEquals(ATTRIBUTE_VALUE, REQUEST.getRequiredAttribute(this.serverWebExchange, ATTRIBUTE_NAME));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getRequiredAttribute(this.serverWebExchange, ATTRIBUTE_NAME));
    }

    @Test
    void testGetRequiredAttributeWithNotFound() {
        assertThrows(IllegalArgumentException.class, () -> REQUEST.getRequiredAttribute(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME));
        assertThrows(IllegalArgumentException.class, () -> SESSION.getRequiredAttribute(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME));
    }

    @Test
    void testGetAttributeOrDefault() {
        assertEquals(ATTRIBUTE_VALUE, REQUEST.getAttributeOrDefault(this.serverWebExchange, ATTRIBUTE_NAME, null));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getAttributeOrDefault(this.serverWebExchange, ATTRIBUTE_NAME, null));

        assertEquals(ATTRIBUTE_VALUE, REQUEST.getAttributeOrDefault(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME, ATTRIBUTE_VALUE));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getAttributeOrDefault(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME, ATTRIBUTE_VALUE));
    }

    @Test
    void testRemoveAttribute() {
        assertSame(ATTRIBUTE_VALUE, REQUEST.removeAttribute(this.serverWebExchange, ATTRIBUTE_NAME));
        assertSame(ATTRIBUTE_VALUE, SESSION.removeAttribute(this.serverWebExchange, ATTRIBUTE_NAME));

        assertNull(REQUEST.removeAttribute(this.serverWebExchange, ATTRIBUTE_NAME));
        assertNull(SESSION.removeAttribute(this.serverWebExchange, ATTRIBUTE_NAME));
    }

    @Test
    void testRemoveAttributeWithNullName() {
        assertNull(REQUEST.removeAttribute(this.serverWebExchange, null));
        assertNull(SESSION.removeAttribute(this.serverWebExchange, null));
    }

    @Test
    void testValueOf() {
        assertEquals(REQUEST, valueOf(SCOPE_REQUEST));
        assertEquals(SESSION, valueOf(SCOPE_SESSION));
    }

    @Test
    void testValueOfWithInvalidScope() {
        assertThrows(IllegalArgumentException.class, () -> valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> valueOf(100));
    }

    @Test
    void testStaticGetAttribute() {
        testStaticGetAttribute(SCOPE_REQUEST);
        testStaticGetAttribute(SCOPE_SESSION);
    }

    @Test
    void testStaticRemoveAttribute() {
        testStaticRemoveAttribute(SCOPE_REQUEST);
        testStaticRemoveAttribute(SCOPE_SESSION);
    }

    @Test
    void testStaticSetAttribute() {
        testStaticSetAttribute(SCOPE_REQUEST);
        testStaticSetAttribute(SCOPE_SESSION);
    }

    @Test
    void testGetAttributeNames() {
        testGetAttributeNames(SCOPE_REQUEST);
        testGetAttributeNames(SCOPE_SESSION);
    }

    void testStaticGetAttribute(int scope) {
        assertSame(ATTRIBUTE_VALUE, getAttribute(this.serverWebExchange, ATTRIBUTE_NAME, scope));
        assertNull(getAttribute(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME, scope));
        assertNull(getAttribute(this.serverWebExchange, null, scope));
    }

    void testStaticSetAttribute(int scope) {
        assertSame(ATTRIBUTE_VALUE, setAttribute(this.serverWebExchange, ATTRIBUTE_NAME, ATTRIBUTE_VALUE, scope));
        assertNull(setAttribute(this.serverWebExchange, ATTRIBUTE_NAME, null, SCOPE_REQUEST));
        assertNull(setAttribute(this.serverWebExchange, null, ATTRIBUTE_VALUE, SCOPE_REQUEST));
    }

    void testStaticRemoveAttribute(int scope) {
        assertSame(ATTRIBUTE_VALUE, removeAttribute(this.serverWebExchange, ATTRIBUTE_NAME, scope));
        assertNull(removeAttribute(this.serverWebExchange, NOT_FOUND_ATTRIBUTE_NAME, scope));
        assertNull(removeAttribute(this.serverWebExchange, null, scope));
    }

    void testGetAttributeNames(int scope) {
        assertNotNull(getAttributeNames(this.serverWebExchange, scope));
    }
}