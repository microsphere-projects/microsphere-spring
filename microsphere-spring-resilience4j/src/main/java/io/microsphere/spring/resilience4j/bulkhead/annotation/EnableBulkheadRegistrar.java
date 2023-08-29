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
package io.microsphere.spring.resilience4j.bulkhead.annotation;

import io.github.resilience4j.bulkhead.configure.BulkheadConfiguration;
import io.microsphere.spring.resilience4j.bulkhead.event.BulkheadApplicationEventPublisher;
import io.microsphere.spring.resilience4j.bulkhead.event.BulkheadEventConsumerBeanRegistrar;
import io.microsphere.spring.resilience4j.bulkhead.web.BulkheadHandlerMethodInterceptor;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.util.ClassLoaderUtils.isPresent;

/**
 * The {@link EnableBulkhead} {@link ImportBeanDefinitionRegistrar} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class EnableBulkheadRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, BulkheadConfiguration.class);
        registerBeanDefinition(registry, BulkheadApplicationEventPublisher.class);
        registerBeanDefinition(registry, BulkheadEventConsumerBeanRegistrar.class);
        if (isPresent("org.springframework.web.servlet.HandlerInterceptor", classLoader)) {
            registerBeanDefinition(registry, BulkheadHandlerMethodInterceptor.class);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
