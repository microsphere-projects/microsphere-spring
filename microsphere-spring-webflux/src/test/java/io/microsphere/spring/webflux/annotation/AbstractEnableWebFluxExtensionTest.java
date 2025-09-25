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

import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webflux.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webflux.method.InterceptingHandlerMethodProcessor;
import io.microsphere.spring.webflux.method.StoringRequestBodyArgumentInterceptor;
import io.microsphere.spring.webflux.method.StoringResponseBodyReturnValueInterceptor;
import io.microsphere.spring.webflux.server.filter.RequestHandledEventPublishingWebFilter;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract {@link EnableWebFluxExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebFluxExtension
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractEnableWebFluxExtensionTest extends AbstractWebFluxTest {

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
        assertEquals(this.publishEvents, isBeanPresent(this.context, RequestHandledEventPublishingWebFilter.class));
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
    protected void testWebEndpoints() {
        this.testHelloWorld();
        this.testGreeting();
        this.testUser();
        this.testError();
        this.testResponseEntity();
        this.testUpdatePerson();
    }
}