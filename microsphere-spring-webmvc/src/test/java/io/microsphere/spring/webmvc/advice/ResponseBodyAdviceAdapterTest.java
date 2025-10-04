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
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * {@link ResponseBodyAdviceAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResponseBodyAdviceAdapter
 * @since 1.0.0
 */
public class ResponseBodyAdviceAdapterTest {

    private MethodParameter returnType;

    private MediaType selectedContentType;

    private ServletServerHttpRequest httpRequest;

    private ServletServerHttpResponse httpResponse;

    private Class<? extends HttpMessageConverter<?>> converterType;

    private ResponseBodyAdviceAdapter adapter;

    @Before
    public void setUp() throws Exception {
        this.returnType = new MethodParameter(getClass().getDeclaredMethod("setUp"), -1);
        this.selectedContentType = APPLICATION_JSON;
        this.converterType = StringHttpMessageConverter.class;
        this.httpRequest = new ServletServerHttpRequest(new MockHttpServletRequest());
        this.httpResponse = new ServletServerHttpResponse(new MockHttpServletResponse());
        this.adapter = new ResponseBodyAdviceAdapter() {
        };
    }

    @Test
    public void testSupports() {
        assertFalse(adapter.supports(returnType, converterType));
    }

    @Test
    public void testBeforeBodyWrite() {
        assertSame(this, adapter.beforeBodyWrite(this, returnType, selectedContentType, converterType, httpRequest, httpResponse));
    }
}