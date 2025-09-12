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

import io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webflux.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webflux.method.InterceptingHandlerMethodProcessor;
import io.microsphere.spring.webflux.method.StoringRequestBodyArgumentInterceptor;
import io.microsphere.spring.webflux.method.StoringResponseBodyReturnValueInterceptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * The {@link ImportBeanDefinitionRegistrar} class for {@link EnableWebFluxExtension Spring WebFlux extensions}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebFluxExtension
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
class WebFluxExtensionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = getLogger(WebFluxExtensionBeanDefinitionRegistrar.class);

    public static final Class<EnableWebFluxExtension> ANNOTATION_CLASS = EnableWebFluxExtension.class;

    public static final String ANNOTATION_CLASS_NAME = ANNOTATION_CLASS.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = getAttributes(metadata);

        registerWebEndpointMappingResolver(attributes, registry);

        registerInterceptingHandlerMethodProcessor(attributes, registry);

        registerStoringRequestBodyArgumentInterceptor(attributes, registry);

        registerStoringResponseBodyReturnValueInterceptor(attributes, registry);

        registerReversedProxyHandlerMapping(attributes, registry);
    }

    private void registerWebEndpointMappingResolver(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean registerWebEndpointMappings = attributes.getBoolean("registerWebEndpointMappings");
        if (registerWebEndpointMappings) {
            registerBeanDefinition(registry, HandlerMappingWebEndpointMappingResolver.class);
        }
        log("@EnableWebFluxExtension.registerWebEndpointMappings = {}", registerWebEndpointMappings);
    }

    private void registerInterceptingHandlerMethodProcessor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean interceptHandlerMethods = attributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            String beanName = InterceptingHandlerMethodProcessor.BEAN_NAME;
            registerBeanDefinition(registry, beanName, InterceptingHandlerMethodProcessor.class);
        }
        log("@EnableWebFluxExtension.interceptHandlerMethods() = {}", interceptHandlerMethods);
    }

    private void registerStoringRequestBodyArgumentInterceptor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean storeRequestBodyArgument = attributes.getBoolean("storeRequestBodyArgument");
        if (storeRequestBodyArgument) {
            registerBeanDefinition(registry, StoringRequestBodyArgumentInterceptor.class);
        }
        log("@EnableWebFluxExtension.storeRequestBodyArgument() = {}", storeRequestBodyArgument);
    }

    private void registerStoringResponseBodyReturnValueInterceptor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean storeResponseBodyReturnValue = attributes.getBoolean("storeResponseBodyReturnValue");
        if (storeResponseBodyReturnValue) {
            registerBeanDefinition(registry, StoringResponseBodyReturnValueInterceptor.class);
        }
        log("@EnableWebFluxExtension.storeResponseBodyReturnValue() = {}", storeResponseBodyReturnValue);
    }

    private void registerReversedProxyHandlerMapping(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean reversedProxyHandlerMapping = attributes.getBoolean("reversedProxyHandlerMapping");
        if (reversedProxyHandlerMapping) {
            registerBeanDefinition(registry, ReversedProxyHandlerMapping.class);
        }
        log("@EnableWebFluxExtension.reversedProxyHandlerMapping() = {}", reversedProxyHandlerMapping);
    }

    private AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        return getAnnotationAttributes(metadata, ANNOTATION_CLASS_NAME);
    }

    private void log(String messagePattern, Object... args) {
        logger.trace(messagePattern, args);
    }
}
