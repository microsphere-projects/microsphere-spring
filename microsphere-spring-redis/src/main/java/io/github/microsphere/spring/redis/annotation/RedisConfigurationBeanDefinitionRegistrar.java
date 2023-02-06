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
package io.github.microsphere.spring.redis.annotation;

import io.github.microsphere.spring.redis.config.RedisConfiguration;
import io.github.microsphere.spring.redis.event.PropagatingRedisConfigurationPropertyChangedEventApplicationListener;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import static io.github.microsphere.spring.redis.event.PropagatingRedisConfigurationPropertyChangedEventApplicationListener.ENVIRONMENT_CHANGE_EVENT_CLASS_NAME;
import static io.github.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link RedisConfiguration} {@link BeanDefinition} Registrar
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisConfigurationBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinitions(registry);
    }

    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        registerRedisConfiguration(registry);
        registerApplicationListeners(registry);
    }

    private void registerRedisConfiguration(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, RedisConfiguration.BEAN_NAME, RedisConfiguration.class);
    }

    private void registerApplicationListeners(BeanDefinitionRegistry registry) {
        if (ClassUtils.isPresent(ENVIRONMENT_CHANGE_EVENT_CLASS_NAME, classLoader)) {
            AbstractBeanDefinition beanDefinition = genericBeanDefinition(PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class).getBeanDefinition();
            BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
