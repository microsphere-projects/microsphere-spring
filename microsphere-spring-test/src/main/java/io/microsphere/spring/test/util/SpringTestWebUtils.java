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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.UrlPathHelper;

import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.collection.MapUtils.of;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpMethod.OPTIONS;

/**
 * Web Utilities class for Spring Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NativeWebRequest
 * @see MockHttpServletRequest
 * @since 1.0.0
 */
public abstract class SpringTestWebUtils {

    public static final String PATH_ATTRIBUTE = UrlPathHelper.class.getName() + ".PATH";

    public static NativeWebRequest createWebRequest() {
        return createWebRequest(r -> {
        });
    }

    public static NativeWebRequest createWebRequest(Consumer<MockHttpServletRequest> requestBuilder) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        requestBuilder.accept(request);
        return new ServletWebRequest(request, response);
    }

    public static NativeWebRequest createWebRequest(String requestURI) {
        return createWebRequest(request -> {
            request.setRequestURI(requestURI);
            request.setAttribute(PATH_ATTRIBUTE, requestURI);
        });
    }

    public static NativeWebRequest createWebRequestWithParams(Object... params) {
        return createWebRequest(request -> {
            request.setParameters(of(params));
        });
    }

    public static NativeWebRequest createWebRequestWithHeaders(Object... headers) {
        return createWebRequestWithHeaders(of(headers));
    }

    public static NativeWebRequest createWebRequestWithHeaders(Map<String, String> headers) {
        return createWebRequest(request -> {
            headers.forEach(request::addHeader);
        });
    }

    public static NativeWebRequest createPreFightRequest() {
        // Create pre-flight request (OPTIONS method with Origin header)
        return createWebRequest(request -> {
            request.setMethod(OPTIONS.name());
            request.addHeader(":METHOD:", request.getMethod());
            request.addHeader(ORIGIN, "*");
            request.addHeader(ACCESS_CONTROL_REQUEST_METHOD, "*");
        });
    }

    private SpringTestWebUtils() {
    }
}
