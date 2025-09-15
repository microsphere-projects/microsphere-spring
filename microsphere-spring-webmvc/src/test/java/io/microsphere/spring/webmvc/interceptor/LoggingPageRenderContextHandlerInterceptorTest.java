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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.Assert.assertTrue;

/**
 * {@link LoggingPageRenderContextHandlerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingPageRenderContextHandlerInterceptor
 * @since 1.0.0
 */
public class LoggingPageRenderContextHandlerInterceptorTest {

    private LoggingPageRenderContextHandlerInterceptor interceptor;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private HandlerMethod handlerMethod;

    private ModelAndView modelAndView;

    @Before
    public void setUp() {
        this.interceptor = new LoggingPageRenderContextHandlerInterceptor();
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        TestController testController = new TestController();
        this.handlerMethod = new HandlerMethod(testController, findMethod(TestController.class, "helloWorld"));
        this.modelAndView = new ModelAndView();
        this.modelAndView.setViewName("test-view");
        this.modelAndView.addObject("name", "value");
    }

    @Test
    public void testPreHandle() throws Exception {
        assertTrue(this.interceptor.preHandle(this.request, this.response, this.handlerMethod));
    }

    @Test
    public void testPostHandle() throws Exception {
        this.interceptor.postHandle(this.request, this.response, (Object) null, this.modelAndView);
    }

    @Test
    public void testPostHandleWithoutModelAndView() throws Exception {
        this.interceptor.postHandle(this.request, this.response, this.handlerMethod, null);
    }

    @Test
    public void testPostHandleWithInvalidModelAndView() throws Exception {
        this.interceptor.postHandle(this.request, this.response, this.handlerMethod, new ModelAndView());
    }

    @Test
    public void testAfterCompletion() throws Exception {
        this.interceptor.afterCompletion(this.request, this.response, this.handlerMethod, null);
    }
}