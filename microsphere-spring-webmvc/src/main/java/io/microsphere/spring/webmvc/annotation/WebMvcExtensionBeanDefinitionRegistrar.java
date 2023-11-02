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
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import static io.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor.BEAN_NAME;
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

    public static final Class<EnableWebMvcExtension> ANNOTATION_CLASS = EnableWebMvcExtension.class;

    public static final String ANNOTATION_CLASS_NAME = ANNOTATION_CLASS.getName();

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
    }

    private void registerInterceptingHandlerMethodProcessor(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean interceptHandlerMethods = attributes.getBoolean("interceptHandlerMethods");
        if (interceptHandlerMethods) {
            String beanName = InterceptingHandlerMethodProcessor.BEAN_NAME;
            registerBeanDefinition(registry, beanName, InterceptingHandlerMethodProcessor.class);
        }
    }

    private AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        return getAnnotationAttributes(metadata, ANNOTATION_CLASS_NAME);
    }

    private void registerHandlerInterceptors(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        Class<? extends HandlerInterceptor>[] interceptorClasses = (Class<? extends HandlerInterceptor>[]) attributes.getClassArray("registerHandlerInterceptors");
        if (!ObjectUtils.isEmpty(interceptorClasses)) {
            registerLazyCompositeHandlerInterceptor(registry, interceptorClasses);
            registerInterceptors(registry, interceptorClasses);
        }
    }

    private void registerLazyCompositeHandlerInterceptor(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>... interceptorClasses) {
        AbstractBeanDefinition beanDefinition = rootBeanDefinition(LazyCompositeHandlerInterceptor.class)
                .addConstructorArgValue(interceptorClasses)
                .getBeanDefinition();
        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }

    private void registerInterceptors(BeanDefinitionRegistry registry, Class<? extends HandlerInterceptor>[] interceptorClasses) {
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
    }

    private void registerStoringResponseBodyReturnValueAdvice(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean storeResponseBodyReturnValue = attributes.getBoolean("storeResponseBodyReturnValue");
        if (storeResponseBodyReturnValue) {
            registerBeanDefinition(registry, StoringResponseBodyReturnValueAdvice.class);
        }
    }
}
