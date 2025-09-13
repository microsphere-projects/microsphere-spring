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

import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.ServletWebEndpointMappingResolver;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.microsphere.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.microsphere.spring.webmvc.advice.StoringResponseBodyReturnValueAdvice;
import io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.microsphere.spring.webmvc.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webmvc.method.support.InterceptingHandlerMethodProcessor;
import io.microsphere.spring.webmvc.test.AbstractWebMvcTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Abstract {@link EnableWebMvcExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
@Ignore
@EnableWebMvc
public abstract class AbstractEnableWebMvcExtensionTest extends AbstractWebMvcTest {

    protected boolean registerWebEndpointMappings;

    protected boolean interceptHandlerMethods;

    protected boolean publishEvents;

    protected boolean registerHandlerInterceptors;

    protected boolean storeRequestBodyArgument;

    protected boolean storeResponseBodyReturnValue;

    protected boolean reversedProxyHandlerMapping;

    @Before
    public void setUp() {
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
    public void testRegisteredBeans() {
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
     * Test the Web Endpoints
     *
     * @see #testHelloWorld()
     * @see #testGreeting()
     * @see #testUser()
     * @see #testError()
     * @see #testResponseEntity()
     */
    @Test
    public void testWebEndpoints() throws Exception {
        this.testHelloWorld();
        this.testGreeting();
        this.testUser();
        this.testError();
        this.testResponseEntity();
    }
}