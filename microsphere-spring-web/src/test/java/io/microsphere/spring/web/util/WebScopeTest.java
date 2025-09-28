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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static io.microsphere.spring.web.util.WebScope.SESSION;
import static io.microsphere.spring.web.util.WebScope.clearAttributes;
import static io.microsphere.spring.web.util.WebScope.getAttribute;
import static io.microsphere.spring.web.util.WebScope.getAttributeNames;
import static io.microsphere.spring.web.util.WebScope.removeAttribute;
import static io.microsphere.spring.web.util.WebScope.setAttribute;
import static io.microsphere.spring.web.util.WebScope.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * {@link WebScope} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebScope
 * @since 1.0.0
 */
class WebScopeTest {

    public static final String ATTRIBUTE_NAME = "test-name";

    public static final String NOT_FOUND_ATTRIBUTE_NAME = "not-found-name";

    public static final String ATTRIBUTE_VALUE = "test-value";

    private MockHttpServletRequest servletRequest;

    private RequestAttributes requestAttributes;

    @BeforeEach
    void setUp() {
        this.servletRequest = new MockHttpServletRequest();
        this.requestAttributes = new ServletWebRequest(this.servletRequest);
        this.servletRequest.setAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
        this.servletRequest.getSession().setAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
    }

    @Test
    void testValue() {
        assertEquals(SCOPE_REQUEST, REQUEST.value());
        assertEquals(SCOPE_SESSION, SESSION.value());
    }

    @Test
    void testGetAttribute() {
        assertEquals(ATTRIBUTE_VALUE, REQUEST.getAttribute(this.requestAttributes, ATTRIBUTE_NAME));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getAttribute(this.requestAttributes, ATTRIBUTE_NAME));

        assertNull(REQUEST.getAttribute(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME));
        assertNull(SESSION.getAttribute(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME));
    }

    @Test
    void testGetAttributeWithNullName() {
        assertNull(REQUEST.setAttribute(this.requestAttributes, null, ATTRIBUTE_VALUE));
        assertNull(SESSION.setAttribute(this.requestAttributes, null, ATTRIBUTE_VALUE));
    }

    @Test
    void testGetAttributeWithNullValue() {
        assertNull(REQUEST.setAttribute(this.requestAttributes, ATTRIBUTE_NAME, null));
        assertNull(SESSION.setAttribute(this.requestAttributes, ATTRIBUTE_NAME, null));
    }

    @Test
    void testGetRequiredAttribute() {
        assertEquals(ATTRIBUTE_VALUE, REQUEST.getRequiredAttribute(this.requestAttributes, ATTRIBUTE_NAME));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getRequiredAttribute(this.requestAttributes, ATTRIBUTE_NAME));
    }

    @Test
    void testGetRequiredAttributeWithNotFound() {
        assertThrows(IllegalArgumentException.class, () -> REQUEST.getRequiredAttribute(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME));
        assertThrows(IllegalArgumentException.class, () -> SESSION.getRequiredAttribute(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME));
    }

    @Test
    void testGetAttributeOrDefault() {
        assertEquals(ATTRIBUTE_VALUE, REQUEST.getAttributeOrDefault(this.requestAttributes, ATTRIBUTE_NAME, null));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getAttributeOrDefault(this.requestAttributes, ATTRIBUTE_NAME, null));

        assertEquals(ATTRIBUTE_VALUE, REQUEST.getAttributeOrDefault(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME, ATTRIBUTE_VALUE));
        assertEquals(ATTRIBUTE_VALUE, SESSION.getAttributeOrDefault(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME, ATTRIBUTE_VALUE));
    }

    @Test
    void testRemoveAttribute() {
        assertSame(ATTRIBUTE_VALUE, REQUEST.removeAttribute(this.requestAttributes, ATTRIBUTE_NAME));
        assertSame(ATTRIBUTE_VALUE, SESSION.removeAttribute(this.requestAttributes, ATTRIBUTE_NAME));

        assertNull(REQUEST.removeAttribute(this.requestAttributes, ATTRIBUTE_NAME));
        assertNull(SESSION.removeAttribute(this.requestAttributes, ATTRIBUTE_NAME));
    }

    @Test
    void testRemoveAttributeWithNullName() {
        assertNull(REQUEST.removeAttribute(this.requestAttributes, null));
        assertNull(SESSION.removeAttribute(this.requestAttributes, null));
    }

    @Test
    void testGetAttributes() {
        assertGetAttributes(REQUEST.getAttributes(this.requestAttributes));
        assertGetAttributes(SESSION.getAttributes(this.requestAttributes));
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

    @Test
    public void testClearAttributes() {
        testStaticGetAttribute(SCOPE_REQUEST);

        clearAttributes(this.requestAttributes, SCOPE_REQUEST);
        assertNull(getAttribute(this.requestAttributes, ATTRIBUTE_NAME, SCOPE_REQUEST));
    }

    @Test
    public void testClearAttributesOnSession() {
        testStaticGetAttribute(SCOPE_SESSION);

        clearAttributes(this.requestAttributes, SCOPE_SESSION);
        assertNull(getAttribute(this.requestAttributes, ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    void assertGetAttributes(Map<String, Object> attributesMap) {
        assertNotNull(attributesMap);
        assertEquals(1, attributesMap.size());
        assertEquals(ATTRIBUTE_VALUE, attributesMap.get(ATTRIBUTE_NAME));
    }

    void testStaticGetAttribute(int scope) {
        assertSame(ATTRIBUTE_VALUE, getAttribute(this.requestAttributes, ATTRIBUTE_NAME, scope));
        assertNull(getAttribute(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME, scope));
        assertNull(getAttribute(this.requestAttributes, null, scope));
    }

    void testStaticSetAttribute(int scope) {
        assertSame(ATTRIBUTE_VALUE, setAttribute(this.requestAttributes, ATTRIBUTE_NAME, ATTRIBUTE_VALUE, scope));
        assertNull(setAttribute(this.requestAttributes, ATTRIBUTE_NAME, null, SCOPE_REQUEST));
        assertNull(setAttribute(this.requestAttributes, null, ATTRIBUTE_VALUE, SCOPE_REQUEST));
    }

    void testStaticRemoveAttribute(int scope) {
        assertSame(ATTRIBUTE_VALUE, removeAttribute(this.requestAttributes, ATTRIBUTE_NAME, scope));
        assertNull(removeAttribute(this.requestAttributes, NOT_FOUND_ATTRIBUTE_NAME, scope));
        assertNull(removeAttribute(this.requestAttributes, null, scope));
    }

    void testGetAttributeNames(int scope) {
        assertNotNull(getAttributeNames(this.requestAttributes, scope));
    }
}