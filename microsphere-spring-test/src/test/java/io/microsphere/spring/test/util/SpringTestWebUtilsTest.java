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


import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createPreFightRequest;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithHeaders;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithParams;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;

;

/**
 * {@link SpringTestWebUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringTestWebUtils
 * @since 1.0.0
 */
public class SpringTestWebUtilsTest {

    @Test
    void testCreateWebRequest() {
        assertNativeWebRequest(createWebRequest());
    }

    @Test
    void testCreateWebRequestWithConsumer() {
        NativeWebRequest webRequest = createWebRequest(request -> {
        });
        assertNativeWebRequest(webRequest);
    }

    @Test
    void testCreateWebRequestWithRequestURI() {
        NativeWebRequest webRequest = createWebRequest("/test");
        assertNativeWebRequest(webRequest);
        MockHttpServletRequest servletRequest = (MockHttpServletRequest) webRequest.getNativeRequest();
        assertTrue("/test".equals(servletRequest.getRequestURI()));
    }

    @Test
    void testCreateWebRequestWithParams() {
        NativeWebRequest webRequest = createWebRequestWithParams("name", "value");
        assertNativeWebRequest(webRequest);
        assertTrue("value".equals(webRequest.getParameter("name")));
    }

    @Test
    void testCreateWebRequestWithHeaders() {
        NativeWebRequest webRequest = createWebRequestWithHeaders("name", "value");
        assertNativeWebRequest(webRequest);
        assertTrue("value".equals(webRequest.getHeader("name")));
    }

    @Test
    void testCreatePreFightRequest() {
        NativeWebRequest webRequest = createPreFightRequest();
        MockHttpServletRequest servletRequest = (MockHttpServletRequest) webRequest.getNativeRequest();
        assertTrue("OPTIONS".equals(servletRequest.getMethod()));
        assertTrue("OPTIONS".equals(servletRequest.getHeader(":METHOD:")));
        assertTrue("*".equals(servletRequest.getHeader(ORIGIN)));
        assertTrue("*".equals(servletRequest.getHeader(ACCESS_CONTROL_REQUEST_METHOD)));
    }

    void assertNativeWebRequest(NativeWebRequest request) {
        assertTrue(request instanceof ServletWebRequest);
        assertTrue(request.getNativeRequest() instanceof MockHttpServletRequest);
    }
}