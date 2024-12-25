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

import io.microsphere.spring.beans.factory.support.AutowireCandidateResolvingListener;
import io.microsphere.spring.core.env.PropertyResolverListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.ConfigurablePropertyResolver;

import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.util.BeanRegistrar.registerBean;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_PREFIX;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_SUFFIX;

/**
 * The listener class for collecting the {@link ConfigurationProperty}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyResolverListener
 * @see AutowireCandidateResolvingListener
 * @see ConfigurationPropertyRepository
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class CollectingConfigurationPropertyListener implements PropertyResolverListener, AutowireCandidateResolvingListener,
        BeanFactoryAware {

    public static final String BEAN_NAME = "collectingConfigurationPropertyListener";

    private BeanFactory beanFactory;

    private ConfigurationPropertyRepository repository;

    private String placeholderPrefix = PLACEHOLDER_PREFIX;

    private String placeholderSuffix = PLACEHOLDER_SUFFIX;

    @Override
    public void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                 Object value, Object defaultValue) {
        ConfigurationProperty configurationProperty = getConfigurationProperty(name);
        configurationProperty.setType(targetType);
        configurationProperty.setValue(value);
        configurationProperty.setDefaultValue(defaultValue);
    }

    @Override
    public void afterGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                         Object value) {
        ConfigurationProperty configurationProperty = getConfigurationProperty(name);
        configurationProperty.setType(targetType);
        configurationProperty.setValue(value);
        configurationProperty.setRequired(true);
    }

    @Override
    public void afterResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
    }

    @Override
    public void afterSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
        this.placeholderPrefix = prefix;
    }

    @Override
    public void afterSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
        this.placeholderSuffix = suffix;
    }

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        Value value = descriptor.getAnnotation(Value.class);
        if (value != null) {

        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
        registerBeanDefinition(registry, ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        registerBean(registry, BEAN_NAME, this);
    }

    private ConfigurationProperty getConfigurationProperty(String name) {
        return this.getRepository().createIfAbsent(name);
    }

    private ConfigurationPropertyRepository getRepository() {
        if (repository == null) {
            repository = this.beanFactory.getBean(ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        }
        return repository;
    }
}
