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

import io.microsphere.spring.web.util.RequestContextStrategy;
import io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webflux.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webflux.method.InterceptingHandlerMethodProcessor;
import io.microsphere.spring.webflux.method.StoringRequestBodyArgumentInterceptor;
import io.microsphere.spring.webflux.method.StoringResponseBodyReturnValueInterceptor;
import io.microsphere.spring.webflux.server.filter.RequestContextWebFilter;
import io.microsphere.spring.webflux.server.filter.RequestHandledEventPublishingWebFilter;
import org.slf4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.uncapitalize;

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

        registerEventPublishingProcessor(attributes, registry);

        registerRequestContextWebFilter(attributes, registry);

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

    private void registerEventPublishingProcessor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean publishEvents = attributes.getBoolean("publishEvents");
        if (publishEvents) {
            registerBeanDefinition(registry, RequestHandledEventPublishingWebFilter.class);
        }
        log("@EnableWebFluxExtension.publishEvents() = {}", publishEvents);
    }

    private void registerRequestContextWebFilter(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        RequestContextStrategy requestContextStrategy = attributes.getEnum("requestContextStrategy");
        Boolean threadContextInheritable;
        switch (requestContextStrategy) {
            case THREAD_LOCAL:
                threadContextInheritable = false;
                break;
            case INHERITABLE_THREAD_LOCAL:
                threadContextInheritable = true;
                break;
            default:
                threadContextInheritable = null;
                break;
        }
        if (threadContextInheritable != null) {
            Class<?> filterClass = RequestContextWebFilter.class;
            BeanDefinition beanDefinition = genericBeanDefinition(filterClass);
            MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
            propertyValues.addPropertyValue("threadContextInheritable", threadContextInheritable);
            String beanName = uncapitalize(filterClass.getSimpleName());
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
        log("@EnableWebFluxExtension.requestContextStrategy() = {}", requestContextStrategy);
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