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
package io.microsphere.spring.resilience4j.timelimiter.annotation;

import io.microsphere.spring.resilience4j.timelimiter.event.TimeLimiterApplicationEventPublisher;
import io.microsphere.spring.resilience4j.timelimiter.event.TimeLimiterEventConsumerBeanRegistrar;
import io.github.resilience4j.timelimiter.configure.TimeLimiterConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;

/**
 * The {@link EnableTimeLimiter} {@link ImportBeanDefinitionRegistrar} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class EnableTimeLimiterRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, TimeLimiterConfiguration.class);
        registerBeanDefinition(registry, TimeLimiterApplicationEventPublisher.class);
        registerBeanDefinition(registry, TimeLimiterEventConsumerBeanRegistrar.class);
    }
}
