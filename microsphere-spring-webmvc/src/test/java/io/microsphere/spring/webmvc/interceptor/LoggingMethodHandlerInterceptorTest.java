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


import io.microsphere.spring.test.web.controller.TestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LoggingMethodHandlerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingMethodHandlerInterceptor
 * @since 1.0.0
 */
class LoggingMethodHandlerInterceptorTest {

    private LoggingMethodHandlerInterceptor interceptor;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private HandlerMethod handlerMethod;

    private ModelAndView modelAndView;

    @BeforeEach
    void setUp() {
        this.interceptor = new LoggingMethodHandlerInterceptor();
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        TestController testController = new TestController();
        this.handlerMethod = new HandlerMethod(testController, findMethod(TestController.class, "helloWorld"));
        this.modelAndView = new ModelAndView();
        this.modelAndView.setViewName("test-view");
        this.modelAndView.addObject("name", "value");
    }

    @Test
    void testPreHandle() throws Exception {
        testPreHandle(this.handlerMethod);
    }

    @Test
    void testPreHandleWithNullHandler() throws Exception {
        testPreHandle(null);
    }

    @Test
    void testPreHandleWithNotHandler() throws Exception {
        testPreHandle(this);
    }

    @Test
    void testPostHandle() throws Exception {
        testPostHandle(this.handlerMethod);
    }

    @Test
    void testPostHandleWithNullHandler() throws Exception {
        testPostHandle(null);
    }

    @Test
    void testPostHandleWithInvalidHandler() throws Exception {
        testPostHandle(this);
    }

    @Test
    void testAfterCompletion() throws Exception {
        testAfterCompletion(this.handlerMethod);
    }

    @Test
    void testAfterCompletionWithNullHandler() throws Exception {
        testAfterCompletion(null);
    }

    @Test
    void testAfterCompletionWithInvalidHandler() throws Exception {
        testAfterCompletion(this);
    }

    @Test
    void testSupports() throws Exception {
        assertTrue(this.interceptor.supports(this.request, this.response, this.handlerMethod));
    }

    void testPreHandle(Object handler) throws Exception {
        assertTrue(this.interceptor.preHandle(this.request, this.response, handler));
        assertTrue(this.interceptor.preHandle(null, this.response, handler));
    }

    void testPostHandle(Object handler) throws Exception {
        this.interceptor.postHandle(this.request, this.response, handler, modelAndView);
        this.interceptor.postHandle(null, this.response, handler, modelAndView);
    }

    void testAfterCompletion(Object handler) throws Exception {
        this.interceptor.afterCompletion(this.request, this.response, handler, null);
        this.interceptor.afterCompletion(this.request, this.response, handler, new Exception("For testing"));

        this.interceptor.afterCompletion(null, this.response, handler, null);
        this.interceptor.afterCompletion(null, this.response, handler, new Exception("For testing"));
    }
}