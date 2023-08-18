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

import io.microsphere.spring.webmvc.advice.StoringHandlerMethodArgumentRequestBodyAdvice;
import io.microsphere.spring.webmvc.advice.StoringHandlerMethodReturnValueResponseBodyAdvice;
import io.microsphere.spring.webmvc.event.RequestMappingHandlerAdapterListener;
import io.microsphere.spring.webmvc.event.WebMvcEventPublisher;
import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor.BEAN_NAME;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} for Spring WebMVC Extension
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
public class WebMvcExtensionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private static final Class<EnableWebMvcExtension> ANNOTATION_CLASS = EnableWebMvcExtension.class;

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        EnableWebMvcExtension enableWebMvcExtension = getEnableWebMvcExtension(metadata);

        registerEventPublishingBeanDefinitions(enableWebMvcExtension, registry);

        registerHandlerInterceptors(enableWebMvcExtension, registry);

        registerStoringHandlerMethodArgumentBeanDefinitions(enableWebMvcExtension, registry);

        registerStoringHandlerMethodReturnValueBeanDefinitions(enableWebMvcExtension, registry);

    }

    private EnableWebMvcExtension getEnableWebMvcExtension(AnnotationMetadata metadata) {
        String annotatedClassName = metadata.getClassName();
        Class<?> annotatedClass = ClassUtils.resolveClassName(annotatedClassName, classLoader);
        return annotatedClass.getAnnotation(ANNOTATION_CLASS);
    }

    private void registerEventPublishingBeanDefinitions(EnableWebMvcExtension enableWebMvcExtension, BeanDefinitionRegistry registry) {
        if (enableWebMvcExtension.publishEvents()) {
            registerBeanDefinition(registry, WebMvcEventPublisher.class);
        }
    }

    private void registerHandlerInterceptors(EnableWebMvcExtension enableWebMvcExtension, BeanDefinitionRegistry registry) {
        Class<? extends HandlerInterceptor>[] interceptorClasses = enableWebMvcExtension.registerHandlerInterceptors();
        if (!ObjectUtils.isEmpty(interceptorClasses)) {
            AbstractBeanDefinition beanDefinition = rootBeanDefinition(LazyCompositeHandlerInterceptor.class)
                    .addConstructorArgValue(interceptorClasses)
                    .getBeanDefinition();
            registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
        }
    }


    private void registerStoringHandlerMethodArgumentBeanDefinitions(EnableWebMvcExtension enableWebMvcExtension, BeanDefinitionRegistry registry) {
        if (enableWebMvcExtension.storeResolvedHandlerMethodArguments()) {
            registerBeanDefinition(registry, StoringHandlerMethodArgumentRequestBodyAdvice.class);
            registerBeanDefinition(registry, RequestMappingHandlerAdapterListener.class);
        }
    }

    private void registerStoringHandlerMethodReturnValueBeanDefinitions(EnableWebMvcExtension enableWebMvcExtension, BeanDefinitionRegistry registry) {
        if (enableWebMvcExtension.storeHandlerMethodReturnValue()) {
            registerBeanDefinition(registry, StoringHandlerMethodReturnValueResponseBodyAdvice.class);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
