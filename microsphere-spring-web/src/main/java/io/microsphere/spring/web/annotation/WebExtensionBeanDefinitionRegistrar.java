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

import io.microsphere.spring.beans.BeanSource;
import io.microsphere.spring.context.annotation.BeanCapableImportCandidate;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.CompositeWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.WebEndpointMappingFilter;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMappingResolver;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

import static io.microsphere.spring.beans.BeanSource.registerBeans;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice.BEAN_NAME;

/**
 * {@link ImportBeanDefinitionRegistrar} for Spring Web Extension
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebExtension
 * @since 1.0.0
 */
public class WebExtensionBeanDefinitionRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        ResolvablePlaceholderAnnotationAttributes<EnableWebExtension> attributes = getAnnotationAttributes(metadata, EnableWebExtension.class);

        BeanSource[] sources = (BeanSource[]) attributes.get("sources");

        registerWebEndpointMappings(attributes, registry, sources);

        registerInterceptHandlers(attributes, registry, sources);

        registerEventPublishingProcessor(attributes, registry);
    }

    private void registerWebEndpointMappings(AnnotationAttributes attributes, BeanDefinitionRegistry registry, BeanSource[] sources) {
        boolean registerWebEndpointMappings = attributes.getBoolean("registerWebEndpointMappings");
        if (registerWebEndpointMappings) {
            registerWebEndpointMappingResolvers(registry, sources);
            registerWebEndpointMappingRegistries(registry, sources);
            registerWebEndpointMappingFactories(registry, sources);
            registerWebEndpointMappingFilters(registry, sources);
            registerWebEndpointMappingRegistrar(registry);
        }
    }

    private void registerWebEndpointMappingResolvers(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, WebEndpointMappingResolver.class);
    }

    private void registerWebEndpointMappingRegistries(BeanDefinitionRegistry registry, BeanSource[] sources) {
        Map<Class<?>, String> beanTypesAndNames = registerBeans(registry, sources, WebEndpointMappingRegistry.class);
        if (beanTypesAndNames.isEmpty()) {
            // If No WebEndpointMappingRegistry bean is registered, register the default SimpleWebEndpointMappingRegistry
            registerBeanDefinition(registry, SimpleWebEndpointMappingRegistry.class, true);
        } else {
            registerBeanDefinition(registry, CompositeWebEndpointMappingRegistry.class, true);
        }
    }

    private void registerWebEndpointMappingFactories(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, WebEndpointMappingFactory.class);
    }

    private void registerWebEndpointMappingFilters(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, WebEndpointMappingFilter.class);
    }

    private void registerWebEndpointMappingRegistrar(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, WebEndpointMappingRegistrar.class);
    }

    private void registerInterceptHandlers(AnnotationAttributes attributes, BeanDefinitionRegistry registry, BeanSource[] sources) {
        boolean interceptHandlerMethods = attributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            registerHandlerMethodAdvices(registry, sources);
            registerHandlerMethodArgumentInterceptors(registry, sources);
            registerHandlerMethodInterceptors(registry, sources);
        }
    }

    private void registerHandlerMethodAdvices(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, HandlerMethodAdvice.class);
        registerDelegatingHandlerMethodAdvice(registry);
    }

    private void registerDelegatingHandlerMethodAdvice(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, BEAN_NAME, DelegatingHandlerMethodAdvice.class);
    }

    private void registerHandlerMethodArgumentInterceptors(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, HandlerMethodArgumentInterceptor.class);
    }

    private void registerHandlerMethodInterceptors(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(registry, sources, HandlerMethodInterceptor.class);
    }

    private void registerEventPublishingProcessor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean publishEvents = attributes.getBoolean("publishEvents");
        if (publishEvents) {
            registerBeanDefinition(registry, WebEventPublisher.class);
        }
    }
}