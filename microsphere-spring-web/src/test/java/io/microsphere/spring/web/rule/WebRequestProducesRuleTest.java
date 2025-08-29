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

package io.microsphere.spring.web.rule;


import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createPreFightRequest;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithHeaders;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebRequestProducesRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class WebRequestProducesRuleTest extends BaseWebRequestRuleTest {

    @Test
    public void testIsPreFlightRequest() {
        WebRequestProducesRule rule = new WebRequestProducesRule();
        NativeWebRequest request = createPreFightRequest();
        assertFalse(rule.matches(request));
    }

    // Test getContent() method
    @Test
    public void testGetContent() {
        // Empty constructor
        WebRequestProducesRule rule1 = new WebRequestProducesRule();
        assertTrue(rule1.getContent().isEmpty());

        // Constructor with parameters
        WebRequestProducesRule rule2 = new WebRequestProducesRule("application/json", "text/plain");
        assertEquals(2, rule2.getContent().size());
    }

    // Test getToStringInfix() method
    @Test
    public void testGetToStringInfix() {
        WebRequestProducesRule rule = new WebRequestProducesRule();
        assertEquals("Should return ' || '", " || ", rule.getToStringInfix());
    }

    // Test pre-flight request returns false
    @Test
    public void testPreFlightRequestReturnsFalse() {
        WebRequestProducesRule rule = new WebRequestProducesRule("application/json");

        // Create pre-flight request (OPTIONS method with Origin header)
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/test");
        request.addHeader("Origin", "http://example.com");
        NativeWebRequest webRequest = new ServletWebRequest(request);

        assertFalse(rule.matches(webRequest));
    }

    // Test empty expressions return false
    @Test
    public void testEmptyExpressionsReturnFalse() {
        WebRequestProducesRule rule = new WebRequestProducesRule();
        NativeWebRequest request = createWebRequestWithHeaders(singletonMap("Accept", "application/json"));
        assertFalse(rule.matches(request));
    }

    // Test media type parsing exception returns false
    @Test
    public void testMediaTypeExceptionReturnsFalse() {
        // Create rule with custom content negotiation manager that throws exception
        ContentNegotiationManager exceptionManager = new ContentNegotiationManager() {
            @Override
            public List<MediaType> resolveMediaTypes(NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {
                throw new HttpMediaTypeNotAcceptableException("Invalid media type");
            }
        };

        WebRequestProducesRule rule = new WebRequestProducesRule(
                new String[]{"application/json"},
                null,
                exceptionManager
        );

        NativeWebRequest request = createWebRequestWithHeaders(singletonMap("Accept", "invalid"));
        assertFalse(rule.matches(request));
    }

    // Test matching expressions return false
    @Test
    public void testMatchingExpressionsReturnFalse() {
        WebRequestProducesRule rule = new WebRequestProducesRule("application/json", "text/plain");

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json"); // Matches first expression

        NativeWebRequest request = createWebRequestWithHeaders(headers);
        assertFalse(rule.matches(request));
    }

    // Test wildcard media type returns false
    @Test
    public void testWildcardMediaTypeReturnsFalse() {
        WebRequestProducesRule rule = new WebRequestProducesRule("application/xml");

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*"); // Wildcard media type

        NativeWebRequest request = createWebRequestWithHeaders(headers);
        assertFalse(rule.matches(request));


        rule = new WebRequestProducesRule("!application/xml");
        assertFalse(rule.matches(request));
    }

    // Test successful match returns true
    @Test
    public void testSuccessfulMatchReturnsTrue() {
        WebRequestProducesRule rule = new WebRequestProducesRule("application/xml");

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json"); // Doesn't match rule expressions

        NativeWebRequest request = createWebRequestWithHeaders(headers);
        assertTrue(rule.matches(request));
    }

    // Test media type caching
    @Test
    public void testMediaTypeCaching() {
        WebRequestProducesRule rule = new WebRequestProducesRule("application/json");

        // Create request with Accept header
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept", "application/json");
        NativeWebRequest webRequest = new ServletWebRequest(request);

        // First call - should parse and cache
        boolean firstResult = rule.matches(webRequest);

        // Modify request to have different Accept header (shouldn't affect cached value)
        request.removeHeader("Accept");
        request.addHeader("Accept", "text/plain");

        // Second call - should use cached media types
        boolean secondResult = rule.matches(webRequest);

        // Both calls should return same result because of caching
        assertEquals(firstResult, secondResult);
    }
}