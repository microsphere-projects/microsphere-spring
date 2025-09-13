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
package io.microsphere.spring.webmvc.annotation;

import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.ServletWebEndpointMappingResolver;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.microsphere.spring.webmvc.advice.StoringResponseBodyReturnValueAdvice;
import io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.microsphere.spring.webmvc.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webmvc.method.support.InterceptingHandlerMethodProcessor;
import io.microsphere.spring.webmvc.test.AbstractWebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collection;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link EnableWebMvcExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractEnableWebMvcExtensionTest extends AbstractWebMvcTest implements HandlerMethodArgumentInterceptor,
        HandlerMethodInterceptor {

    protected static final String expectedArgument0 = "hello";

    protected boolean registerWebEndpointMappings;

    protected boolean interceptHandlerMethods;

    protected boolean publishEvents;

    protected boolean registerHandlerInterceptors;

    protected boolean storeRequestBodyArgument;

    protected boolean storeResponseBodyReturnValue;

    protected boolean reversedProxyHandlerMapping;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        EnableWebMvcExtension enableWebMvcExtension = this.getClass().getAnnotation(EnableWebMvcExtension.class);
        this.registerWebEndpointMappings = enableWebMvcExtension.registerWebEndpointMappings();
        this.interceptHandlerMethods = enableWebMvcExtension.interceptHandlerMethods();
        this.publishEvents = enableWebMvcExtension.publishEvents();
        this.registerHandlerInterceptors = enableWebMvcExtension.registerHandlerInterceptors() ? true :
                isNotEmpty(enableWebMvcExtension.handlerInterceptors());
        this.storeRequestBodyArgument = enableWebMvcExtension.storeRequestBodyArgument();
        this.storeResponseBodyReturnValue = enableWebMvcExtension.storeResponseBodyReturnValue();
        this.reversedProxyHandlerMapping = enableWebMvcExtension.reversedProxyHandlerMapping();
    }

    @Test
    void testRegisteredBeans() {
        assertTrue(isBeanPresent(this.context, WebMvcExtensionConfiguration.class));
        // From @EnableWebExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, SimpleWebEndpointMappingRegistry.class));
        assertEquals(this.interceptHandlerMethods, this.context.containsBean(DelegatingHandlerMethodAdvice.BEAN_NAME));
        assertEquals(this.publishEvents, isBeanPresent(this.context, WebEventPublisher.class));
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, WebEndpointMappingRegistrar.class));

        // From @EnableWebMvcExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, ServletWebEndpointMappingResolver.class));
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, HandlerMappingWebEndpointMappingResolver.class));
        assertEquals(this.interceptHandlerMethods, isBeanPresent(this.context, DelegatingHandlerMethodAdvice.class));
        assertEquals(this.interceptHandlerMethods, this.context.containsBean(InterceptingHandlerMethodProcessor.BEAN_NAME));
        assertEquals(this.interceptHandlerMethods, isBeanPresent(this.context, InterceptingHandlerMethodProcessor.class));
        assertEquals(this.registerHandlerInterceptors, isBeanPresent(this.context, LazyCompositeHandlerInterceptor.class));
        assertEquals(this.storeRequestBodyArgument, isBeanPresent(this.context, StoringRequestBodyArgumentAdvice.class));
        assertEquals(this.storeResponseBodyReturnValue, isBeanPresent(this.context, StoringResponseBodyReturnValueAdvice.class));
        assertEquals(this.reversedProxyHandlerMapping, isBeanPresent(this.context, ReversedProxyHandlerMapping.class));
    }

    /**
     * Test the Web Endpoint with single parameter
     *
     * @see #testGreeting()
     * @see #testUser()
     * @see #testView()
     * @see #testUpdatePerson()
     */
    @Test
    void testWebEndpoints() throws Exception {
        this.testGreeting();
        this.testUser();
        this.testError();
        this.testUpdatePerson();
    }

    @EventListener(WebEndpointMappingsReadyEvent.class)
    void onWebEndpointMappingsReadyEvent(WebEndpointMappingsReadyEvent event) {
        Collection<WebEndpointMapping> mappings = event.getMappings();
        WebEndpointMapping webEndpointMapping = mappings.iterator().next();
        String[] patterns = webEndpointMapping.getPatterns();
        assertEquals(1, patterns.length);
    }

    @EventListener(HandlerMethodArgumentsResolvedEvent.class)
    public void onHandlerMethodArgumentsResolvedEvent(HandlerMethodArgumentsResolvedEvent event) {
        Method method = event.getMethod();
        HandlerMethod handlerMethod = event.getHandlerMethod();
        assertEquals(method, handlerMethod.getMethod());
        assertHandlerMethod(handlerMethod);
        Object[] arguments = event.getArguments();
        assertArguments(arguments);
    }

    @Override
    public void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest) throws Exception {
        assertHandlerMethod(handlerMethod);
        assertNativeWebRequest(webRequest);
    }

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod, NativeWebRequest webRequest) throws Exception {
        // Reuse
        beforeResolveArgument(parameter, handlerMethod, webRequest);
    }

    @Override
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
        assertHandlerMethod(handlerMethod);
        assertArguments(args);
    }

    @Override
    public void afterExecute(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) throws Exception {
        beforeExecute(handlerMethod, args, request);
        if (returnValue == null) {
            assertNotNull(error);
            assertEquals(expectedArgument0, error.getMessage());
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

    protected void assertArguments(Object[] arguments) {
        assertEquals(1, arguments.length);
    }

    protected void assertReturnValue(Object returnValue) {
        assertNotNull(returnValue);
    }

    protected void assertNativeWebRequest(NativeWebRequest webRequest) {
        assertNotNull(webRequest);
    }
}