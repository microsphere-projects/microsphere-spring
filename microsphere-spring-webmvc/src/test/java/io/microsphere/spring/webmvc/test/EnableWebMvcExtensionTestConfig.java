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

package io.microsphere.spring.webmvc.test;

import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EnableWebMvcExtension} Test Config
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
public class EnableWebMvcExtensionTestConfig implements HandlerMethodArgumentInterceptor, HandlerMethodInterceptor {

    @EventListener(WebEndpointMappingsReadyEvent.class)
    public void onWebEndpointMappingsReadyEvent(WebEndpointMappingsReadyEvent event) {
        Collection<WebEndpointMapping> mappings = event.getMappings();
        assertTrue(mappings.size() > 0);
    }

    @EventListener(HandlerMethodArgumentsResolvedEvent.class)
    public void onHandlerMethodArgumentsResolvedEvent(HandlerMethodArgumentsResolvedEvent event) {
        Method method = event.getMethod();
        HandlerMethod handlerMethod = event.getHandlerMethod();
        assertEquals(method, handlerMethod.getMethod());
        assertHandlerMethod(handlerMethod);
        Object[] arguments = event.getArguments();
        assertArguments(method, arguments);
    }

    @Override
    public void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest) {
        assertHandlerMethod(handlerMethod);
        assertNativeWebRequest(webRequest);
    }

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod, NativeWebRequest webRequest) {
        // Reuse
        beforeResolveArgument(parameter, handlerMethod, webRequest);
    }

    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) {
        assertHandlerMethod(handlerMethod);
        assertArguments(handlerMethod.getMethod(), args);
    }

    @Override
    public void afterExecute(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) {
        beforeExecute(handlerMethod, args, request);
        if (returnValue == null) {
            assertNotNull(error);
        } else {
            assertReturnValue(returnValue);
            assertNull(error);
        }
    }

    protected void assertHandlerMethod(HandlerMethod handlerMethod) {
        assertNotNull(handlerMethod);
        Object bean = handlerMethod.getBean();
        assertNotNull(bean);
        assertEquals(TestController.class, handlerMethod.getBeanType());
    }

    protected void assertArguments(Method method, Object[] arguments) {
        assertEquals(method.getParameterCount(), arguments.length);
    }

    protected void assertReturnValue(Object returnValue) {
        assertNotNull(returnValue);
    }

    protected void assertNativeWebRequest(NativeWebRequest webRequest) {
        assertNotNull(webRequest);
    }
}
