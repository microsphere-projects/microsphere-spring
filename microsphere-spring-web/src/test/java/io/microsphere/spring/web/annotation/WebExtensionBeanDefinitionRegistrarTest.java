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

package io.microsphere.spring.web.annotation;


import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import org.junit.jupiter.api.Test;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebExtensionBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebExtensionBeanDefinitionRegistrar
 * @since 1.0.0
 */
class WebExtensionBeanDefinitionRegistrarTest {


    @EnableWebExtension
    static class DefaultConfig {
    }

    @EnableWebExtension(registerWebEndpointMappings = false)
    static class DisableWebEndpointMappingConfig {
    }

    @EnableWebExtension(interceptHandlerMethods = false)
    static class DisableInterceptHandlerMethodsConfig {
    }

    @EnableWebExtension(publishEvents = false)
    static class DisablePublishEventConfig {
    }


    @Test
    void testDefaultConfig() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, SimpleWebEndpointMappingRegistry.class));
            assertTrue(isBeanPresent(context, DelegatingHandlerMethodAdvice.class));
            assertTrue(isBeanPresent(context, WebEventPublisher.class));
        }, DefaultConfig.class);
    }

    @Test
    void testDisableWebEndpointMappingsConfig() {
        testInSpringContainer(context -> {
            assertFalse(isBeanPresent(context, SimpleWebEndpointMappingRegistry.class));
            assertTrue(isBeanPresent(context, DelegatingHandlerMethodAdvice.class));
            assertTrue(isBeanPresent(context, WebEventPublisher.class));
        }, DisableWebEndpointMappingConfig.class);
    }

    @Test
    void testDisableInterceptHandlerMethodsConfig() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, SimpleWebEndpointMappingRegistry.class));
            assertFalse(isBeanPresent(context, DelegatingHandlerMethodAdvice.class));
            assertTrue(isBeanPresent(context, WebEventPublisher.class));
        }, DisableInterceptHandlerMethodsConfig.class);
    }

    @Test
    void testDisablePublishEventConfig() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, SimpleWebEndpointMappingRegistry.class));
            assertTrue(isBeanPresent(context, DelegatingHandlerMethodAdvice.class));
            assertFalse(isBeanPresent(context, WebEventPublisher.class));
        }, DisablePublishEventConfig.class);
    }
}