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
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.Assert.assertTrue;

/**
 * Abstract {@link HandlerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerInterceptor
 * @since 1.0.0
 */
public abstract class AbstractHandlerInterceptorTest {

    protected HandlerInterceptor interceptor;

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    protected ModelAndView modelAndView;

    @Before
    public void setUp() {
        this.interceptor = createHandlerInterceptor();
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.modelAndView = new ModelAndView();
        this.modelAndView.setViewName("test-view");
        this.modelAndView.addObject("name", "value");
    }

    protected abstract HandlerInterceptor createHandlerInterceptor();

    protected Object getHandler() {
        TestController testController = new TestController();
        return new HandlerMethod(testController, findMethod(TestController.class, "helloWorld"));
    }

    @Test
    public void testPreHandle() throws Exception {
        testPreHandle(getHandler());
    }

    @Test
    public void testPreHandleWithNullHandler() throws Exception {
        testPreHandle(null);
    }

    @Test
    public void testPreHandleWithNotHandler() throws Exception {
        testPreHandle(this);
    }

    @Test
    public void testPostHandle() throws Exception {
        testPostHandle(getHandler());
    }

    @Test
    public void testPostHandleWithNullHandler() throws Exception {
        testPostHandle(null);
    }

    @Test
    public void testPostHandleWithInvalidHandler() throws Exception {
        testPostHandle(this);
    }

    @Test
    public void testAfterCompletion() throws Exception {
        testAfterCompletion(getHandler());
    }

    @Test
    public void testAfterCompletionWithNullHandler() throws Exception {
        testAfterCompletion(null);
    }

    @Test
    public void testAfterCompletionWithInvalidHandler() throws Exception {
        testAfterCompletion(this);
    }

    protected void testPreHandle(Object handler) throws Exception {
        assertTrue(this.interceptor.preHandle(this.request, this.response, handler));
        assertTrue(this.interceptor.preHandle(null, this.response, handler));
    }

    protected void testPostHandle(Object handler) throws Exception {
        this.interceptor.postHandle(this.request, this.response, handler, modelAndView);
        this.interceptor.postHandle(null, this.response, handler, modelAndView);
    }

    protected void testAfterCompletion(Object handler) throws Exception {
        this.interceptor.afterCompletion(this.request, this.response, handler, null);
        this.interceptor.afterCompletion(this.request, this.response, handler, new Exception("For testing"));

        this.interceptor.afterCompletion(null, this.response, handler, null);
        this.interceptor.afterCompletion(null, this.response, handler, new Exception("For testing"));
    }
}