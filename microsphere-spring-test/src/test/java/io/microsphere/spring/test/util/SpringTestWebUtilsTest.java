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

package io.microsphere.spring.test.util;


import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static io.microsphere.spring.test.util.SpringTestWebUtils.clearAttributes;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createPreFightRequest;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithHeaders;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithParams;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * {@link SpringTestWebUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringTestWebUtils
 * @since 1.0.0
 */
public class SpringTestWebUtilsTest {

    static final String TEST_ATTRIBUTE_NAME = "test-name";

    static final String TEST_ATTRIBUTE_VALUE = "test-value";

    @Test
    public void testCreateWebRequest() {
        assertNativeWebRequest(createWebRequest());
    }

    @Test
    public void testCreateWebRequestWithConsumer() {
        NativeWebRequest webRequest = createWebRequest(request -> {
        });
        assertNativeWebRequest(webRequest);
    }

    @Test
    public void testCreateWebRequestWithRequestURI() {
        NativeWebRequest webRequest = createWebRequest("/test");
        assertNativeWebRequest(webRequest);
        MockHttpServletRequest servletRequest = (MockHttpServletRequest) webRequest.getNativeRequest();
        assertTrue("/test".equals(servletRequest.getRequestURI()));
    }

    @Test
    public void testCreateWebRequestWithParams() {
        NativeWebRequest webRequest = createWebRequestWithParams("name", "value");
        assertNativeWebRequest(webRequest);
        assertTrue("value".equals(webRequest.getParameter("name")));
    }

    @Test
    public void testCreateWebRequestWithHeaders() {
        NativeWebRequest webRequest = createWebRequestWithHeaders("name", "value");
        assertNativeWebRequest(webRequest);
        assertTrue("value".equals(webRequest.getHeader("name")));
    }

    @Test
    public void testCreatePreFightRequest() {
        NativeWebRequest webRequest = createPreFightRequest();
        MockHttpServletRequest servletRequest = (MockHttpServletRequest) webRequest.getNativeRequest();
        assertTrue("OPTIONS".equals(servletRequest.getMethod()));
        assertTrue("OPTIONS".equals(servletRequest.getHeader(":METHOD:")));
        assertTrue("*".equals(servletRequest.getHeader(ORIGIN)));
        assertTrue("*".equals(servletRequest.getHeader(ACCESS_CONTROL_REQUEST_METHOD)));
    }

    @Test
    public void testClearAttributes() {
        NativeWebRequest webRequest = createWebRequest(request -> {
            request.setAttribute(TEST_ATTRIBUTE_NAME, TEST_ATTRIBUTE_VALUE);
        });
        assertSame(TEST_ATTRIBUTE_VALUE, webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_REQUEST));

        clearAttributes(webRequest);
        assertNull(webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_REQUEST));
    }

    @Test
    public void testClearAttributesOnEmptyAttributes() {
        NativeWebRequest webRequest = createWebRequest();
        assertNull(webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_REQUEST));

        clearAttributes(webRequest);
        assertNull(webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_REQUEST));
    }

    @Test
    public void testClearAttributesForSession() {
        NativeWebRequest webRequest = createWebRequest(request -> {
            request.getSession().setAttribute(TEST_ATTRIBUTE_NAME, TEST_ATTRIBUTE_VALUE);
        });

        assertNull(webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_REQUEST));
        assertSame(TEST_ATTRIBUTE_VALUE, webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_SESSION));

        clearAttributes(webRequest, SCOPE_SESSION);
        assertNull(webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_REQUEST));
        assertNull(webRequest.getAttribute(TEST_ATTRIBUTE_NAME, SCOPE_SESSION));
    }

    void assertNativeWebRequest(NativeWebRequest request) {
        assertTrue(request instanceof ServletWebRequest);
        assertTrue(request.getNativeRequest() instanceof MockHttpServletRequest);
    }
}