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

import io.microsphere.spring.context.annotation.AnnotatedBeanCapableImportCandidate;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.microsphere.spring.web.annotation.EnableWebExtension;
import io.microsphere.spring.web.annotation.WebExtensionBeanDefinitionRegistrar;
import io.microsphere.spring.web.metadata.ServletWebEndpointMappingResolver;
import io.microsphere.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.microsphere.spring.webmvc.advice.StoringResponseBodyReturnValueAdvice;
import io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.microsphere.spring.webmvc.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webmvc.method.support.InterceptingHandlerMethodProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.servlet.HandlerInterceptor;

import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor.BEAN_NAME;
import static io.microsphere.util.ArrayUtils.arrayEquals;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} for Spring WebMVC Extension
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvcExtension
 * @see EnableWebExtension
 * @see WebExtensionBeanDefinitionRegistrar
 * @see WebMvcExtensionConfiguration
 * @see ServletWebEndpointMappingResolver
 * @see HandlerMappingWebEndpointMappingResolver
 * @see InterceptingHandlerMethodProcessor
 * @see HandlerInterceptor
 * @see StoringRequestBodyArgumentAdvice
 * @see StoringResponseBodyReturnValueAdvice
 * @see ReversedProxyHandlerMapping
 * @since 1.0.0
 */
class WebMvcExtensionBeanDefinitionRegistrar extends AnnotatedBeanCapableImportCandidate<EnableWebMvcExtension> {

    private static final Class<? extends HandlerInterceptor>[] ALL_HANDLER_INTERCEPTOR_CLASSES = ofArray(HandlerInterceptor.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                        BeanNameGenerator importBeanNameGenerator,
                                        ResolvablePlaceholderAnnotationAttributes<EnableWebMvcExtension> annotationAttributes) {

        registerWebMvcExtensionConfiguration(registry);

        registerWebEndpointMappingResolvers(annotationAttributes, registry);

        registerInterceptingHandlerMethodProcessor(annotationAttributes, registry);

        registerHandlerInterceptors(annotationAttributes, registry);

        registerStoringRequestBodyArgumentAdvice(annotationAttributes, registry);

        registerStoringResponseBodyReturnValueAdvice(annotationAttributes, registry);

        registerReversedProxyHandlerMapping(annotationAttributes, registry);
    }

    private void registerWebMvcExtensionConfiguration(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, WebMvcExtensionConfiguration.class);
    }

    private void registerWebEndpointMappingResolvers(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean registerWebEndpointMappings = annotationAttributes.getBoolean("registerWebEndpointMappings");
        if (registerWebEndpointMappings) {
            registerBeanDefinition(registry, ServletWebEndpointMappingResolver.class);
            registerBeanDefinition(registry, HandlerMappingWebEndpointMappingResolver.class);
        }
        log("@EnableWebMvcExtension.registerWebEndpointMappings = {}", registerWebEndpointMappings);
    }

    private void registerInterceptingHandlerMethodProcessor(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean interceptHandlerMethods = annotationAttributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            String beanName = InterceptingHandlerMethodProcessor.BEAN_NAME;
            registerBeanDefinition(registry, beanName, InterceptingHandlerMethodProcessor.class);
        }
        log("@EnableWebMvcExtension.interceptHandlerMethods() = {}", interceptHandlerMethods);
    }

    private void registerHandlerInterceptors(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        Class<? extends HandlerInterceptor>[] interceptorClasses = resolveHandlerInterceptorClasses(annotationAttributes);
        if (isNotEmpty(interceptorClasses)) {
            registerLazyCompositeHandlerInterceptor(registry, interceptorClasses);
            registerInterceptors(registry, interceptorClasses);
        }
    }

    private Class<? extends HandlerInterceptor>[] resolveHandlerInterceptorClasses(AnnotationAttributes annotationAttributes) {
        boolean registerHandlerInterceptors = annotationAttributes.getBoolean("registerHandlerInterceptors");
        Class<? extends HandlerInterceptor>[] handlerInterceptors =
                (Class<? extends HandlerInterceptor>[]) annotationAttributes.getClassArray("handlerInterceptors");
        Class<? extends HandlerInterceptor>[] handlerInterceptorClasses = registerHandlerInterceptors ?
                ALL_HANDLER_INTERCEPTOR_CLASSES : handlerInterceptors;
        log("@EnableWebMvcExtension.registerHandlerInterceptors() = {} , handlerInterceptors() = {} , handlerInterceptorClasses = {}",
                registerHandlerInterceptors, handlerInterceptors, handlerInterceptorClasses);
        return handlerInterceptorClasses;
    }

    private void registerLazyCompositeHandlerInterceptor(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>... interceptorClasses) {
        AbstractBeanDefinition beanDefinition = rootBeanDefinition(LazyCompositeHandlerInterceptor.class)
                .addConstructorArgValue(interceptorClasses)
                .getBeanDefinition();
        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }

    private void registerInterceptors(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>[] interceptorClasses) {
        if (arrayEquals(ALL_HANDLER_INTERCEPTOR_CLASSES, interceptorClasses)) {
            return;
        }
        for (Class<? extends HandlerInterceptor> interceptorClass : interceptorClasses) {
            registerInterceptor(registry, interceptorClass);
        }
    }

    private void registerInterceptor(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor> interceptorClass) {
        registerBeanDefinition(registry, interceptorClass);
    }

    private void registerStoringRequestBodyArgumentAdvice(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean storeRequestBodyArgument = annotationAttributes.getBoolean("storeRequestBodyArgument");
        if (storeRequestBodyArgument) {
            registerBeanDefinition(registry, StoringRequestBodyArgumentAdvice.class);
        }
        log("@EnableWebMvcExtension.storeRequestBodyArgument() = {}", storeRequestBodyArgument);
    }

    private void registerStoringResponseBodyReturnValueAdvice(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean storeResponseBodyReturnValue = annotationAttributes.getBoolean("storeResponseBodyReturnValue");
        if (storeResponseBodyReturnValue) {
            registerBeanDefinition(registry, StoringResponseBodyReturnValueAdvice.class);
        }
        log("@EnableWebMvcExtension.storeResponseBodyReturnValue() = {}", storeResponseBodyReturnValue);
    }

    private void registerReversedProxyHandlerMapping(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean reversedProxyHandlerMapping = annotationAttributes.getBoolean("reversedProxyHandlerMapping");
        if (reversedProxyHandlerMapping) {
            registerBeanDefinition(registry, ReversedProxyHandlerMapping.class);
        }
        log("@EnableWebMvcExtension.reversedProxyHandlerMapping() = {}", reversedProxyHandlerMapping);
    }

    private void log(String messagePattern, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(messagePattern, args);
        }
    }
}