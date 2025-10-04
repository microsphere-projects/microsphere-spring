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

package io.microsphere.spring.web.util;


import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodArguments;
import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodReturnValue;
import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodReturnValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.core.MethodParameter.forExecutable;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;
import static org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes;
import static org.springframework.web.context.request.RequestContextHolder.setRequestAttributes;

/**
 * {@link RequestAttributesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttributesUtils
 * @since 1.0.0
 */
class RequestAttributesUtilsTest {

    private MockHttpServletRequest servletRequest;

    private RequestAttributes requestAttributes;

    private HandlerMethod handlerMethod;

    private Method method;

    private MethodParameter methodParameter;

    private User user;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        this.servletRequest = new MockHttpServletRequest();
        this.requestAttributes = new ServletWebRequest(this.servletRequest);
        setRequestAttributes(this.requestAttributes);
        this.handlerMethod = new HandlerMethod(new TestController(), "user", User.class);
        this.method = this.handlerMethod.getMethod();
        this.methodParameter = forExecutable(this.method, 0);
        this.user = new User();
        this.user.setName("Mercy");
        this.user.setAge(18);
    }

    @AfterEach
    void tearDown() {
        resetRequestAttributes();
    }

    @Test
    void testSetHandlerMethodRequestBodyArgument() {
        setHandlerMethodRequestBodyArgument(this.method, this.user);
        assertEquals(this.user, getHandlerMethodRequestBodyArgument(this.method));
    }

    @Test
    void testSetHandlerMethodRequestBodyArgumentWithHandlerMethod() throws NoSuchMethodException {
        setHandlerMethodRequestBodyArgument(this.handlerMethod.getMethod(), this.user);
        assertEquals(this.user, getHandlerMethodRequestBodyArgument(this.handlerMethod));
    }

    @Test
    void testSetHandlerMethodRequestBodyArgumentWithNull() {
        setHandlerMethodRequestBodyArgument(null, this.method, this.user);
        setHandlerMethodRequestBodyArgument(getRequestAttributes(), null, this.user);
        setHandlerMethodRequestBodyArgument(getRequestAttributes(), this.method, null);
    }

    @Test
    void testGetHandlerMethodArgumentsWithHandlerMethod() {
        assertHandlerMethodArguments(getHandlerMethodArguments(this.handlerMethod));
        assertHandlerMethodArguments(getHandlerMethodArguments(this.handlerMethod));
    }

    @Test
    void testGetHandlerMethodArgumentsWithMethod() {
        assertHandlerMethodArguments(getHandlerMethodArguments(this.method));
        assertHandlerMethodArguments(getHandlerMethodArguments(this.method));
    }

    @Test
    void testGetHandlerMethodArgumentsWithMethodParameter() {
        assertHandlerMethodArguments(getHandlerMethodArguments(this.methodParameter));
        assertHandlerMethodArguments(getHandlerMethodArguments(this.methodParameter));
    }

    @Test
    void testSetHandlerMethodReturnValue() {
        setHandlerMethodReturnValue(this.method, this.user);
        assertEquals(this.user, getHandlerMethodReturnValue(this.method));
    }

    @Test
    void testGetHandlerMethodReturnValueWithHandlerMethod() {
        setHandlerMethodReturnValue(this.method, this.user);
        assertEquals(this.user, getHandlerMethodReturnValue(this.handlerMethod));
    }

    @Test
    void testSetHandlerMethodReturnValueWithNull() {
        setHandlerMethodReturnValue(null, this.method, this.user);
        setHandlerMethodReturnValue(getRequestAttributes(), null, this.user);
        setHandlerMethodReturnValue(getRequestAttributes(), this.method, null);
    }

    void assertHandlerMethodArguments(Object[] arguments) {
        assertEquals(1, arguments.length);
    }
}