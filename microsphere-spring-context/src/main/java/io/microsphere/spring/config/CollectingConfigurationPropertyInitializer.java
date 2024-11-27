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
package io.microsphere.spring.config;

import io.microsphere.spring.core.env.PropertyResolverListener;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static io.microsphere.spring.config.ConfigurationPropertyRepository.BEAN_NAME;
import static io.microsphere.spring.util.BeanRegistrar.registerBean;
import static io.microsphere.spring.util.SpringFactoriesLoaderUtils.registerFactories;

/**
 * The Initializer for Collecting Configuration Property
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see CollectingConfigurationPropertyListener
 * @see ConfigurationProperty
 * @see ApplicationContextInitializer
 * @since 1.0.0
 */
public class CollectingConfigurationPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        // Load PropertyResolverListener SPI and then be registered as the bean
        registerFactories(beanFactory, PropertyResolverListener.class);
        // Register ConfigurationPropertyRepository
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        registerBean(registry, BEAN_NAME, ConfigurationPropertyRepository.class);
    }
}
