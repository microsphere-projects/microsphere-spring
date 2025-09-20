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

package io.microsphere.spring.webmvc.interceptor;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AbstractPageRenderContextHandlerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractPageRenderContextHandlerInterceptor
 * @since 1.0.0
 */
class AbstractPageRenderContextHandlerInterceptorTest {

    private static final String TEST_CONTENT = "test-content";

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private AbstractPageRenderContextHandlerInterceptor interceptor;

    @BeforeEach
    void setUp() {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.interceptor = new AbstractPageRenderContextHandlerInterceptor() {
            @Override
            protected void postHandleOnPageRenderContext(HttpServletRequest request, HttpServletResponse response,
                                                         Object handler, ModelAndView modelAndView) throws Exception {
                response.getWriter().write(TEST_CONTENT);
            }
        };
    }

    @Test
    void testPostHandle() throws Exception {
        this.interceptor.postHandle(request, response, null, null);
        assertEquals(EMPTY_STRING, this.response.getContentAsString());
    }

    @Test
    void testPostHandleOnPageRenderContext() throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("test-view");
        this.interceptor.postHandle(request, response, null, modelAndView);
        assertEquals(TEST_CONTENT, response.getContentAsString());
    }
}