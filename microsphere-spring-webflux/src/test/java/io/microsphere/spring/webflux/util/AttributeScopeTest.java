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

package io.microsphere.spring.webflux.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebSession;

import java.util.Map;

import static io.microsphere.spring.webflux.util.AttributeScope.REQUEST;
import static io.microsphere.spring.webflux.util.AttributeScope.SESSION;
import static io.microsphere.spring.webflux.util.AttributeScope.getAttribute;
import static io.microsphere.spring.webflux.util.AttributeScope.getAttributeNames;
import static io.microsphere.spring.webflux.util.AttributeScope.removeAttribute;
import static io.microsphere.spring.webflux.util.AttributeScope.setAttribute;
import static io.microsphere.spring.webflux.util.AttributeScope.valueOf;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.mock.web.server.MockServerWebExchange.from;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * {@link AttributeScope} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AttributeScope
 * @since 1.0.0
 */
public class AttributeScopeTest {

    public static final String ATTRIBUTE_NAME = "test-name";

    public static final String NOT_FOUND_ATTRIBUTE_NAME = "not-found-name";

    public static final String ATTRIBUTE_VALUE = "test-value";

    public static final String HEADER_NAME = "test-header-name";

    public static final String HEADER_VALUE = "test-header-value";

    public static final String HEADER_NAME_2 = "test-header-name-2";

    public static final String[] HEADER_VALUE_2 = ofArray("test-header-value-2", "test-header-value-3");

    public static final String PARAM_NAME = "test-param-name";

    public static final String PARAM_VALUE = "test-param-value";

    public static final String PARAM_NAME_2 = "test-param-name-2";

    public static final String[] PARAM_VALUE_2 = ofArray("test-param-value-2", "test-param-value-3");

    private MockServerWebExchange serverWebExchange;

    @BeforeEach
    void setUp() {
        this.serverWebExchange = mockServerWebExchange();
    }

    public static MockServerWebExchange mockServerWebExchange() {
        MockServerHttpRequest request = get("/test")
                .header(HEADER_NAME, HEADER_VALUE)
                .header(HEADER_NAME_2, HEADER_VALUE_2)
                .queryParam(PARAM_NAME, PARAM_VALUE)
                .queryParam(PARAM_NAME_2, PARAM_VALUE_2)
                .build();
        MockServerWebExchange serverWebExchange = from(request);
        Map<String, Object> attributes = serverWebExchange.getAttributes();
        attributes.put(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

        WebSession webSession = serverWebExchange.getSession().block();
        webSession.getAttributes().put(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
        return serverWebExchange;
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