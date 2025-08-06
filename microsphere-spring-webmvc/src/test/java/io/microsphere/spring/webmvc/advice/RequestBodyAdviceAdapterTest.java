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

package io.microsphere.spring.webmvc.advice;


import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.lang.reflect.Type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

/**
 * {@link RequestBodyAdviceAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestBodyAdviceAdapter
 * @since 1.0.0
 */
public class RequestBodyAdviceAdapterTest {

    private ServletServerHttpRequest httpRequest;

    private MethodParameter methodParameter;

    private Type targetType;

    private Class<? extends HttpMessageConverter<?>> converterType;

    private RequestBodyAdviceAdapter adapter;

    @Before
    public void setUp() throws Exception {
        this.httpRequest = new ServletServerHttpRequest(new MockHttpServletRequest());
        this.methodParameter = new MethodParameter(getClass().getMethod("setUp"), -1);
        this.targetType = methodParameter.getGenericParameterType();
        this.converterType = StringHttpMessageConverter.class;
        this.adapter = new RequestBodyAdviceAdapter() {
        };
    }

    @Test
    public void testSupports() {
        assertFalse(adapter.supports(methodParameter, targetType, converterType));
    }

    @Test
    public void testBeforeBodyRead() throws IOException {
        assertSame(httpRequest, adapter.beforeBodyRead(httpRequest, methodParameter, targetType, converterType));
    }

    @Test
    public void testAfterBodyRead() {
        assertSame(this, adapter.afterBodyRead(this, httpRequest, methodParameter, targetType, converterType));
    }

    @Test
    public void testHandleEmptyBody() {
        assertSame(this, adapter.handleEmptyBody(this, httpRequest, methodParameter, targetType, converterType));
    }
}