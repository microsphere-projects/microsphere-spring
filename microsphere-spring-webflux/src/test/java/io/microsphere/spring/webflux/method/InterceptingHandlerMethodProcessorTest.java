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

package io.microsphere.spring.webflux.method;


import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.webflux.annotation.AbstractEnableWebFluxExtensionTest;
import io.microsphere.spring.webflux.annotation.EnableWebFluxExtension;
import io.microsphere.spring.webflux.context.request.ServerWebRequest;
import io.microsphere.spring.webflux.test.RouterFunctionTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerResult;

import java.lang.reflect.Method;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.webflux.test.WebTestUtils.mockServerWebExchange;
import static io.microsphere.util.ClassUtils.getTypes;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.core.MethodParameter.forExecutable;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * {@link InterceptingHandlerMethodProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InterceptingHandlerMethodProcessor
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        InterceptingHandlerMethodProcessorTest.class,
        RouterFunctionTestConfig.class
})
@EnableWebFluxExtension
class InterceptingHandlerMethodProcessorTest extends AbstractEnableWebFluxExtensionTest {

    @Autowired
    private InterceptingHandlerMethodProcessor processor;

    @Autowired
    private TestController testController;

    private Method greetingMethod;

    private HandlerMethod greetingHandlerMethod;

    private MethodParameter greetingMethodParameter0;

    @BeforeEach
    void setUp() {
        this.greetingMethod = findMethod(TestController.class, "greeting", String.class);
        this.greetingHandlerMethod = new HandlerMethod(testController, greetingMethod);
        this.greetingMethodParameter0 = forExecutable(greetingMethod, 0);
    }

    @Test
    void testSupportsParameter() {
        assertTrue(processor.supportsParameter(greetingMethodParameter0));
    }

    @Test
    void testSupportsParameterWithUnsupportedMethodParameter() {
        Method helloWorldMethod = findMethod(TestController.class, "helloWorld");
        MethodParameter methodParameter = forExecutable(helloWorldMethod, -1);
        assertFalse(processor.supportsParameter(methodParameter));
    }

    @Test
    void testSupportsWithResponseBodyResult() {
        HandlerResult handlerResult = newHandlerResult("helloWorld");
        assertTrue(processor.supports(handlerResult));
    }

    @Test
    void testSupportsWithResponseEntityResult() {
        HandlerResult handlerResult = newHandlerResult("responseEntity");
        assertTrue(processor.supports(handlerResult));
    }

    @Test
    void testSupportsWithServerResponseResult() {
        HandlerResult handlerResult = new HandlerResult(this, ok().bodyValue("OK").block(), greetingMethodParameter0);
        assertTrue(processor.supports(handlerResult));
    }

    @Test
    void testSupportsWithModelAndViewResult() {
        HandlerResult handlerResult = newHandlerResult("view");
        assertTrue(processor.supports(handlerResult));
    }

    @Test
    void testSupportsWithUnsupportedHandlerResult() {
        Method method = findMethod(Object.class, "hashCode");
        HandlerResult handlerResult = new HandlerResult(method, null, forExecutable(method, -1));
        assertFalse(processor.supports(handlerResult));
    }

    @Test
    void testResolveArgumentResolver() {
        assertNull(processor.resolveArgumentResolver(greetingMethodParameter0, emptyList()));
    }

    @Test
    void testResolveArguments() {
        NativeWebRequest webRequest = new ServerWebRequest(mockServerWebExchange());
        assertNull(processor.resolveArguments(webRequest, greetingMethodParameter0, null));
    }

    HandlerResult newHandlerResult(String handlerMethodName, Object... arguments) {
        Class[] parameterTypes = getTypes(arguments);
        Method method = findMethod(TestController.class, handlerMethodName, parameterTypes);
        HandlerMethod handlerMethod = new HandlerMethod(testController, method);
        Object returnValue = invokeMethod(testController, method, arguments);
        return new HandlerResult(handlerMethod, returnValue, handlerMethod.getReturnType());
    }
}