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

package io.microsphere.spring.webmvc.method.support;


import io.microsphere.spring.test.web.controller.TestController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;

/**
 * {@link LoggingHandlerMethodArgumentResolverAdvice} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingHandlerMethodArgumentResolverAdvice
 * @since 1.0.0
 */
public class LoggingHandlerMethodArgumentResolverAdviceTest {

    private LoggingHandlerMethodArgumentResolverAdvice advice;

    private MethodParameter greetingMethodParameter0;

    private Object resolvedArgument;

    private ModelAndViewContainer mavContainer;

    private NativeWebRequest webRequest;

    private WebDataBinderFactory binderFactory;

    @Before
    public void setUp() {
        this.advice = new LoggingHandlerMethodArgumentResolverAdvice();
        Method greetingMethod = findMethod(TestController.class, "greeting", String.class);
        this.greetingMethodParameter0 = new MethodParameter(greetingMethod, 0);
        this.resolvedArgument = "Testing";
        this.mavContainer = new ModelAndViewContainer();
        this.webRequest = createWebRequest();
        this.binderFactory = new DefaultDataBinderFactory(null);
    }

    @Test
    public void testBeforeResolveArgument() {
        this.advice.beforeResolveArgument(this.greetingMethodParameter0, this.mavContainer, this.webRequest, this.binderFactory);
    }

    @Test
    public void testAfterResolveArgument() {
        this.advice.afterResolveArgument(this.greetingMethodParameter0, resolvedArgument, this.mavContainer, this.webRequest, this.binderFactory);
    }
}