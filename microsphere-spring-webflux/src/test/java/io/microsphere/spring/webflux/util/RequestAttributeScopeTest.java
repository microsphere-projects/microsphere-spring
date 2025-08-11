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

import static io.microsphere.spring.webflux.util.RequestAttributeScope.REQUEST;
import static io.microsphere.spring.webflux.util.RequestAttributeScope.SESSION;
import static io.microsphere.spring.webflux.util.RequestAttributeScope.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.get;
import static org.springframework.mock.web.server.MockServerWebExchange.from;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * {@link RequestAttributeScope} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttributeScope
 * @since 1.0.0
 */
class RequestAttributeScopeTest {

    private static final String ATTRIBUTE_NAME = "test-name";

    private static final String NOT_FOUND_ATTRIBUTE_NAME = "not-found-name";

    private static final String ATTRIBUTE_VALUE = "test-value";

    private MockServerWebExchange serverWebExchange;

    @BeforeEach
    void setUp() {
        MockServerHttpRequest request = get("/test").build();
        this.serverWebExchange = from(request);
        Map<String, Object> attributes = this.serverWebExchange.getAttributes();
        attributes.put(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

        WebSession webSession = this.serverWebExchange.getSession().block();
        webSession.getAttributes().put(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
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
    void testValueOf() {
        assertEquals(REQUEST, valueOf(SCOPE_REQUEST));
        assertEquals(SESSION, valueOf(SCOPE_SESSION));
    }

    @Test
    void testValueOfWithInvalidScope() {
        assertThrows(IllegalArgumentException.class, () -> valueOf(-1));
        assertThrows(IllegalArgumentException.class, () -> valueOf(100));
    }
}