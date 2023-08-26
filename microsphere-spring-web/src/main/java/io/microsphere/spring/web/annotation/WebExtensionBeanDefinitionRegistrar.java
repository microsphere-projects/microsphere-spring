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

import io.microsphere.spring.web.event.EventPublishingHandlerMethodInterceptor;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * {@link ImportBeanDefinitionRegistrar} for Spring Web Extension
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebExtension
 * @since 1.0.0
 */
public class WebExtensionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private static final Class<EnableWebExtension> ANNOTATION_CLASS = EnableWebExtension.class;

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = getAttributes(metadata);

        boolean interceptHandlerMethods = attributes.getBoolean("interceptHandlerMethods");

        registerInterceptingHandlerMethodProcessor(interceptHandlerMethods, registry);

        registerEventPublishingBeanDefinitions(attributes, interceptHandlerMethods, registry);
    }

    private AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        return fromMap(metadata.getAnnotationAttributes(ANNOTATION_CLASS.getName()));
    }

    private void registerInterceptingHandlerMethodProcessor(boolean interceptHandlerMethods, BeanDefinitionRegistry registry) {
        if (interceptHandlerMethods) {
            String beanName = DelegatingHandlerMethodAdvice.BEAN_NAME;
            registerBeanDefinition(registry, beanName, DelegatingHandlerMethodAdvice.class);
        }
    }

    private EnableWebExtension getEnableWebMvcExtension(AnnotationMetadata metadata) {
        String annotatedClassName = metadata.getClassName();
        Class<?> annotatedClass = ClassUtils.resolveClassName(annotatedClassName, classLoader);
        return annotatedClass.getAnnotation(ANNOTATION_CLASS);
    }

    private void registerEventPublishingBeanDefinitions(AnnotationAttributes attributes,
                                                        boolean interceptHandlerMethods, BeanDefinitionRegistry registry) {
        boolean publishEvents = attributes.getBoolean("publishEvents");
        if (publishEvents) {
            if (interceptHandlerMethods) {
                registerBeanDefinition(registry, EventPublishingHandlerMethodInterceptor.class);
            }
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
