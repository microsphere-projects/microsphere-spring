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


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createPreFightRequest;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.util.WebRequestUtils.PATH_ATTRIBUTE;
import static io.microsphere.spring.web.util.WebRequestUtils.addCookie;
import static io.microsphere.spring.web.util.WebRequestUtils.addHeader;
import static io.microsphere.spring.web.util.WebRequestUtils.getBestMatchingHandler;
import static io.microsphere.spring.web.util.WebRequestUtils.getBestMatchingPattern;
import static io.microsphere.spring.web.util.WebRequestUtils.getContentType;
import static io.microsphere.spring.web.util.WebRequestUtils.getCookieValue;
import static io.microsphere.spring.web.util.WebRequestUtils.getHeader;
import static io.microsphere.spring.web.util.WebRequestUtils.getHeaderValues;
import static io.microsphere.spring.web.util.WebRequestUtils.getMatrixVariables;
import static io.microsphere.spring.web.util.WebRequestUtils.getMethod;
import static io.microsphere.spring.web.util.WebRequestUtils.getPathWithinHandlerMapping;
import static io.microsphere.spring.web.util.WebRequestUtils.getProducibleMediaTypes;
import static io.microsphere.spring.web.util.WebRequestUtils.getRequestBody;
import static io.microsphere.spring.web.util.WebRequestUtils.getResolvedLookupPath;
import static io.microsphere.spring.web.util.WebRequestUtils.getUriTemplateVariables;
import static io.microsphere.spring.web.util.WebRequestUtils.hasBody;
import static io.microsphere.spring.web.util.WebRequestUtils.isPreFlightRequest;
import static io.microsphere.spring.web.util.WebRequestUtils.parseContentType;
import static io.microsphere.spring.web.util.WebRequestUtils.setHeader;
import static io.microsphere.spring.web.util.WebRequestUtils.writeResponseBody;
import static io.microsphere.spring.web.util.WebSourceTest.testName;
import static io.microsphere.spring.web.util.WebSourceTest.testValue;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

/**
 * {@link WebRequestUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestUtils
 * @since 1.0.0
 */
class WebRequestUtilsTest {

    private MockHttpServletRequest servletRequest;

    private ServletWebRequest request;

    @BeforeEach
    void setUp() {
        this.servletRequest = new MockHttpServletRequest();
        this.request = new ServletWebRequest(this.servletRequest);
    }

    @Test
    void testGetMethod() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(":METHOD:", "POST"));
        assertEquals("POST", getMethod(request));

        request = createWebRequest(r -> r.setMethod("POST"));
        assertEquals("POST", getMethod(request));
    }

    @Test
    void testGetMethodOnNotHttpServletRequest() {
        NativeWebRequest request = mock(NativeWebRequest.class);
        assertNull(getMethod(request));
    }

    @Test
    void testIsPreFlightRequest() {
        NativeWebRequest request = createPreFightRequest();
        assertTrue(isPreFlightRequest(request));

        request = createWebRequest();
        assertFalse(isPreFlightRequest(request));

        request = createWebRequest(r -> r.setMethod("OPTIONS"));
        assertFalse(isPreFlightRequest(request));

        request = createWebRequest(r -> {
            r.setMethod("OPTIONS");
            r.addHeader(ORIGIN, "");
        });
        assertFalse(isPreFlightRequest(request));
    }

    @Test
    void testGetContentType() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));
        assertEquals(APPLICATION_JSON_VALUE, getContentType(request));
    }

    @Test
    void testParseContentType() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));
        assertEquals(APPLICATION_JSON, parseContentType(request));

        request = createWebRequest(r -> r.addHeader(CONTENT_TYPE, "test"));
        assertNull(parseContentType(request));

        assertSame(APPLICATION_OCTET_STREAM, parseContentType(this.request));
    }

    @Test
    void testHasBody() {
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
    void testGetResolvedLookupPath() {
        NativeWebRequest request = createWebRequest(r -> r.setAttribute(PATH_ATTRIBUTE, SLASH));
        assertEquals(SLASH, getResolvedLookupPath(request));
    }

    @Test
    void testGetHeader() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(testName, testValue));
        assertEquals(testValue, getHeader(request, testName));
    }

    @Test
    void testGetHeaderValues() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(testName, testValue));
        assertArrayEquals(ofArray(testValue), getHeaderValues(request, testName));
    }

    @Test
    void testSetHeader() {
        NativeWebRequest request = createWebRequest();
        setHeader(request, testName, testValue);
        HttpServletResponse response = request.getNativeResponse(HttpServletResponse.class);
        assertEquals(testValue, response.getHeader(testName));
    }

    @Test
    void testAddHeader() {
        NativeWebRequest request = createWebRequest();
        addHeader(request, testName, testValue);
        HttpServletResponse response = request.getNativeResponse(HttpServletResponse.class);
        assertEquals(testValue, response.getHeader(testName));
        assertEquals(ofList(testValue), response.getHeaders(testName));

        String value = "test";
        addHeader(request, testName, value);
        assertEquals(testValue, response.getHeader(testName));
        assertEquals(ofList(testValue, value), response.getHeaders(testName));
    }

    @Test
    void testGetCookieValue() {
        NativeWebRequest request = createWebRequest(r -> r.setCookies(new Cookie(testName, testValue)));
        assertEquals(testValue, getCookieValue(request, testName));
    }

    @Test
    void testAddCookie() {
        NativeWebRequest request = createWebRequest();
        addCookie(request, testName, testValue);
        MockHttpServletResponse response = request.getNativeResponse(MockHttpServletResponse.class);
        Cookie[] cookies = response.getCookies();
        assertEquals(testValue, cookies[0].getValue());
    }

    @Test
    void testGetRequestBody() {
        NativeWebRequest request = createWebRequest();
        assertNull(getRequestBody(request, Object.class));
    }

    @Test
    void testWriteResponseBody() {
        NativeWebRequest request = createWebRequest();
        assertThrows(UnsupportedOperationException.class, () -> writeResponseBody(request, testName, testValue));
    }

    @Test
    void testGetBestMatchingHandler() {
        assertNull(getBestMatchingHandler(this.request));
    }

    @Test
    void testGetPathWithinHandlerMapping() {
        assertNull(getPathWithinHandlerMapping(this.request));
    }

    @Test
    void testGetBestMatchingPattern() {
        assertNull(getBestMatchingPattern(this.request));
    }

    @Test
    void testGetUriTemplateVariables() {
        assertNull(getUriTemplateVariables(this.request));
    }

    @Test
    void testGetMatrixVariables() {
        assertNull(getMatrixVariables(this.request));
    }

    @Test
    void testGetProducibleMediaTypes() {
        assertNull(getProducibleMediaTypes(this.request));
    }
}