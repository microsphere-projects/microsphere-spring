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
package io.microsphere.spring.webflux.annotation;

import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webflux.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webflux.method.InterceptingHandlerMethodProcessor;
import io.microsphere.spring.webflux.method.StoringRequestBodyArgumentInterceptor;
import io.microsphere.spring.webflux.method.StoringResponseBodyReturnValueInterceptor;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link EnableWebFluxExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebFluxExtension
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractEnableWebFluxExtensionTest extends AbstractWebFluxTest implements
        HandlerMethodArgumentInterceptor, HandlerMethodInterceptor {

    protected boolean registerWebEndpointMappings;

    protected boolean interceptHandlerMethods;

    protected boolean publishEvents;

    protected boolean storeRequestBodyArgument;

    protected boolean storeResponseBodyReturnValue;

    protected boolean reversedProxyHandlerMapping;

    @BeforeEach
    protected void setup() {
        EnableWebFluxExtension enableWebFluxExtension = this.getClass().getAnnotation(EnableWebFluxExtension.class);
        this.registerWebEndpointMappings = enableWebFluxExtension.registerWebEndpointMappings();
        this.interceptHandlerMethods = enableWebFluxExtension.interceptHandlerMethods();
        this.publishEvents = enableWebFluxExtension.publishEvents();
        this.storeRequestBodyArgument = enableWebFluxExtension.storeRequestBodyArgument();
        this.storeResponseBodyReturnValue = enableWebFluxExtension.storeResponseBodyReturnValue();
        this.reversedProxyHandlerMapping = enableWebFluxExtension.reversedProxyHandlerMapping();
    }

    @Test
    void testRegisteredBeans() {
        // From @EnableWebExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, SimpleWebEndpointMappingRegistry.class));
        assertEquals(this.interceptHandlerMethods, this.context.containsBean(DelegatingHandlerMethodAdvice.BEAN_NAME));
        assertEquals(this.publishEvents, isBeanPresent(this.context, WebEventPublisher.class));
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, WebEndpointMappingRegistrar.class));

        // From @EnableWebFluxExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, HandlerMappingWebEndpointMappingResolver.class));
        assertEquals(this.interceptHandlerMethods, isBeanPresent(this.context, DelegatingHandlerMethodAdvice.class));
        assertEquals(this.interceptHandlerMethods, isBeanPresent(this.context, InterceptingHandlerMethodProcessor.class));
        assertEquals(this.storeRequestBodyArgument, isBeanPresent(this.context, StoringRequestBodyArgumentInterceptor.class));
        assertEquals(this.storeResponseBodyReturnValue, isBeanPresent(this.context, StoringResponseBodyReturnValueInterceptor.class));
        assertEquals(this.reversedProxyHandlerMapping, isBeanPresent(this.context, ReversedProxyHandlerMapping.class));
    }

    /**
     * Test the Web Endpoints
     *
     * @see #testHelloWorld()
     * @see #testGreeting()
     * @see #testUser()
     * @see #testError()
     * @see #testResponseEntity()
     * @see #testUpdatePerson()
     */
    @Test
    void testWebEndpoints() {
        this.testHelloWorld();
        this.testGreeting();
        this.testUser();
        this.testError();
        this.testResponseEntity();
        this.testUpdatePerson();
    }

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