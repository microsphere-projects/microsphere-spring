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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} for Spring Web Extension
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebExtension
 * @since 1.0.0
 */
public class WebExtensionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    public static final Class<EnableWebExtension> ANNOTATION_CLASS = EnableWebExtension.class;

    public static final String ANNOTATION_CLASS_NAME = ANNOTATION_CLASS.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = getAttributes(metadata);

        registerWebEndpointMappingRegistry(attributes, registry);

        registerDelegatingHandlerMethodAdvice(attributes, registry);

        registerEventPublishingProcessor(attributes, registry);

    }

    private AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        return getAnnotationAttributes(metadata, ANNOTATION_CLASS_NAME);
    }

    private void registerWebEndpointMappingRegistry(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean registerWebEndpointMappings = attributes.getBoolean("registerWebEndpointMappings");
        if (registerWebEndpointMappings) {
            registerBeanDefinition(registry, SimpleWebEndpointMappingRegistry.class);
        }
    }

    private void registerDelegatingHandlerMethodAdvice(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean interceptHandlerMethods = attributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            String beanName = DelegatingHandlerMethodAdvice.BEAN_NAME;
            registerBeanDefinition(registry, beanName, DelegatingHandlerMethodAdvice.class);
        }
    }

    private void registerEventPublishingProcessor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean publishEvents = attributes.getBoolean("publishEvents");
        if (publishEvents) {
            registerBeanDefinition(registry, WebEventPublisher.class);
        }
    }
}
