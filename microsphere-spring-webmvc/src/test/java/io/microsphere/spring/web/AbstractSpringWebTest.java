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

package io.microsphere.spring.web;

import io.microsphere.lang.function.ThrowableBiConsumer;
import io.microsphere.lang.function.ThrowableConsumer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.spring.web.util.WebRequestUtils.PATH_ATTRIBUTE;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpMethod.OPTIONS;

/**
 * Abstract Spring Web Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NativeWebRequest
 * @see MockHttpServletRequest
 * @since 1.0.0
 */
public class AbstractSpringWebTest {
    protected NativeWebRequest createWebRequest() {
        return createWebRequest(r -> {
        });
    }

    protected NativeWebRequest createWebRequest(Consumer<MockHttpServletRequest> requestBuilder) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        requestBuilder.accept(request);
        return new ServletWebRequest(request);
    }

    protected NativeWebRequest createWebRequest(String requestURI) {
        return createWebRequest(request -> {
            request.setRequestURI(requestURI);
            request.setAttribute(PATH_ATTRIBUTE, requestURI);
        });
    }

    protected NativeWebRequest createWebRequestWithParams(Object... params) {
        return createWebRequest(request -> {
            request.setParameters(of(params));
        });
    }

    protected NativeWebRequest createWebRequestWithHeaders(Object... headers) {
        return createWebRequestWithHeaders(of(headers));
    }

    protected NativeWebRequest createWebRequestWithHeaders(Map<String, String> headers) {
        return createWebRequest(request -> {
            headers.forEach(request::addHeader);
        });
    }

    protected NativeWebRequest createPreFightRequest() {
        // Create pre-flight request (OPTIONS method with Origin header)
        return createWebRequest(request -> {
            request.setMethod(OPTIONS.name());
            request.addHeader(":METHOD:", request.getMethod());
            request.addHeader(ORIGIN, "http://example.com");
            request.addHeader(ACCESS_CONTROL_REQUEST_METHOD, "POST");
        });
    }

    protected void testInSpringContainer(ThrowableConsumer<ConfigurableApplicationContext> consumer, Class<?>... configClasses) {
        testInSpringContainer((context, environment) -> {
            consumer.accept(context);
        }, configClasses);
    }

    protected void testInSpringContainer(ThrowableBiConsumer<ConfigurableApplicationContext, ConfigurableEnvironment> consumer, Class<?>... configClasses) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(configClasses);
        try {
            consumer.accept(context, context.getEnvironment());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        context.close();
    }
}
