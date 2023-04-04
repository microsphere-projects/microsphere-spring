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
package io.github.microsphere.spring.resilience4j.circuitbreaker.annotation;

import io.github.microsphere.spring.resilience4j.circuitbreaker.event.CircuitBreakerApplicationEventPublisher;
import io.github.microsphere.spring.resilience4j.circuitbreaker.event.CircuitBreakerEventConsumerRegistrar;
import io.github.microsphere.spring.resilience4j.circuitbreaker.webmvc.CircuitBreakerHandlerInterceptor;
import io.github.resilience4j.circuitbreaker.configure.CircuitBreakerConfiguration;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import static io.github.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;

/**
 * The {@link EnableCircuitBreaker} {@link ImportBeanDefinitionRegistrar} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class EnableCircuitBreakerRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, CircuitBreakerConfiguration.class);
        registerBeanDefinition(registry, CircuitBreakerApplicationEventPublisher.class);
        registerBeanDefinition(registry, CircuitBreakerEventConsumerRegistrar.class);
        if (ClassUtils.isPresent("org.springframework.web.servlet.HandlerInterceptor", classLoader)) {
            registerBeanDefinition(registry, CircuitBreakerHandlerInterceptor.class);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
