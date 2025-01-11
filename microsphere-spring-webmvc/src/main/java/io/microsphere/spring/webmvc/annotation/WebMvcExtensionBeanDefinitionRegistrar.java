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

import io.microsphere.logging.Logger;
import io.microsphere.spring.web.annotation.EnableWebExtension;
import io.microsphere.spring.web.annotation.WebExtensionBeanDefinitionRegistrar;
import io.microsphere.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.microsphere.spring.webmvc.advice.StoringResponseBodyReturnValueAdvice;
import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.microsphere.spring.webmvc.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.webmvc.method.support.InterceptingHandlerMethodProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor.BEAN_NAME;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.of;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} for Spring WebMVC Extension
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvcExtension
 * @see EnableWebExtension
 * @see WebExtensionBeanDefinitionRegistrar
 * @since 1.0.0
 */
public class WebMvcExtensionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = getLogger(WebMvcExtensionBeanDefinitionRegistrar.class);

    public static final Class<EnableWebMvcExtension> ANNOTATION_CLASS = EnableWebMvcExtension.class;

    public static final String ANNOTATION_CLASS_NAME = ANNOTATION_CLASS.getName();

    private static final Class<? extends HandlerInterceptor>[] ALL_HANDLER_INTERCEPTOR_CLASSES = of(HandlerInterceptor.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = getAttributes(metadata);

        registerWebEndpointMappingRegistrar(attributes, registry);

        registerInterceptingHandlerMethodProcessor(attributes, registry);

        registerHandlerInterceptors(attributes, registry);

        registerStoringRequestBodyArgumentAdvice(attributes, registry);

        registerStoringResponseBodyReturnValueAdvice(attributes, registry);

    }

    private void registerWebEndpointMappingRegistrar(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean registerWebEndpointMappings = attributes.getBoolean("registerWebEndpointMappings");
        if (registerWebEndpointMappings) {
            registerBeanDefinition(registry, WebEndpointMappingRegistrar.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.registerWebEndpointMappings = {}", registerWebEndpointMappings);
        }
    }

    private void registerInterceptingHandlerMethodProcessor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean interceptHandlerMethods = attributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            String beanName = InterceptingHandlerMethodProcessor.BEAN_NAME;
            registerBeanDefinition(registry, beanName, InterceptingHandlerMethodProcessor.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.interceptHandlerMethods() = {}", interceptHandlerMethods);
        }
    }

    private AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        return getAnnotationAttributes(metadata, ANNOTATION_CLASS_NAME);
    }

    private void registerHandlerInterceptors(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        Class<? extends HandlerInterceptor>[] interceptorClasses = resolveHandlerInterceptorClasses(attributes);
        if (isNotEmpty(interceptorClasses)) {
            registerLazyCompositeHandlerInterceptor(registry, interceptorClasses);
            registerInterceptors(registry, interceptorClasses);
        }
    }

    private Class<? extends HandlerInterceptor>[] resolveHandlerInterceptorClasses(AnnotationAttributes attributes) {
        boolean registerHandlerInterceptors = attributes.getBoolean("registerHandlerInterceptors");
        Class<? extends HandlerInterceptor>[] handlerInterceptors =
                (Class<? extends HandlerInterceptor>[]) attributes.getClassArray("handlerInterceptors");
        Class<? extends HandlerInterceptor>[] handlerInterceptorClasses = registerHandlerInterceptors ?
                ALL_HANDLER_INTERCEPTOR_CLASSES : handlerInterceptors;
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.registerHandlerInterceptors() = {} , handlerInterceptors() = {} , handlerInterceptorClasses = {}",
                    registerHandlerInterceptors, handlerInterceptors, handlerInterceptorClasses);
        }
        return handlerInterceptorClasses;
    }

    private void registerLazyCompositeHandlerInterceptor(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>... interceptorClasses) {
        AbstractBeanDefinition beanDefinition = rootBeanDefinition(LazyCompositeHandlerInterceptor.class)
                .addConstructorArgValue(interceptorClasses)
                .getBeanDefinition();
        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }

    private void registerInterceptors(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>[] interceptorClasses) {
        if (Arrays.equals(ALL_HANDLER_INTERCEPTOR_CLASSES, interceptorClasses)) {
            return;
        }
        for (Class<? extends HandlerInterceptor> interceptorClass : interceptorClasses) {
            registerInterceptor(registry, interceptorClass);
        }
    }

    private void registerInterceptor(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor> interceptorClass) {
        registerBeanDefinition(registry, interceptorClass);
    }

    private void registerStoringRequestBodyArgumentAdvice(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean storeRequestBodyArgument = attributes.getBoolean("storeRequestBodyArgument");
        if (storeRequestBodyArgument) {
            registerBeanDefinition(registry, StoringRequestBodyArgumentAdvice.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.storeRequestBodyArgument() = {}", storeRequestBodyArgument);
        }
    }

    private void registerStoringResponseBodyReturnValueAdvice(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean storeResponseBodyReturnValue = attributes.getBoolean("storeResponseBodyReturnValue");
        if (storeResponseBodyReturnValue) {
            registerBeanDefinition(registry, StoringResponseBodyReturnValueAdvice.class);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("@EnableWebMvcExtension.storeResponseBodyReturnValue() = {}", storeResponseBodyReturnValue);
        }
    }
}
