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

import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.spring.beans.factory.support.AutowireCandidateResolvingListener;
import io.microsphere.spring.core.env.PropertyResolverListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.ConfigurablePropertyResolver;

import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_PREFIX;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_SUFFIX;

/**
 * A listener implementation that collects and manages configuration properties during property resolution and autowiring processes.
 *
 * <p>{@link CollectingConfigurationPropertyListener} integrates with Spring's property resolution and autowire candidate resolving mechanisms to:
 * <ul>
 *     <li>Capture configuration properties as they are resolved by a {@link ConfigurablePropertyResolver}</li>
 *     <li>Track metadata such as the property name, target type, value, default value, and whether it's required</li>
 *     <li>Register itself as a Spring bean for integration into the application context lifecycle</li>
 * </ul>
 *
 * <h3>Property Resolution Example</h3>
 * When a property is resolved via a {@link ConfigurablePropertyResolver}, this listener records its details:
 *
 * <pre>{@code
 * @Component
 * public class MyPropertyResolverListener implements PropertyResolverListener {
 *     @Override
 *     public void afterGetProperty(ConfigurablePropertyResolver resolver, String name, Class<?> targetType, Object value, Object defaultValue) {
 *         System.out.println("Captured property: " + name);
 *         System.out.println("Target type: " + targetType.getName());
 *         System.out.println("Value: " + value);
 *         System.out.println("Default Value: " + defaultValue);
 *     }
 * }
 * }</pre>
 *
 * <h3>Autowire Candidate Resolving Example</h3>
 * This listener also participates in autowire candidate resolution events:
 *
 * <pre>{@code
 * @Component
 * public class MyAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {
 *     @Override
 *     public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
 *         System.out.println("Suggested value for dependency " + descriptor + ": " + suggestedValue);
 *     }
 *
 *     @Override
 *     public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
 *         System.out.println("Lazy proxy created for " + descriptor + " in bean " + beanName);
 *     }
 * }
 * }</pre>
 *
 * <h3>Bean Registration</h3>
 * This class is typically registered as a Spring bean using:
 *
 * <pre>{@code
 * BeanDefinitionRegistry registry = ...;
 * registerBean(registry, CollectingConfigurationPropertyListener.BEAN_NAME, new CollectingConfigurationPropertyListener());
 * }</pre>
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
    public void afterSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
        this.placeholderPrefix = prefix;
    }

    @Override
    public void afterSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
        this.placeholderSuffix = suffix;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
        registerBeanDefinition(registry, ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        registerBean(registry, BEAN_NAME, this);
    }

    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }

    public String getPlaceholderSuffix() {
        return placeholderSuffix;
    }

    private ConfigurationProperty getConfigurationProperty(String name) {
        ConfigurationPropertyRepository repository = getRepository();
        return repository.createIfAbsent(name);
    }

    private ConfigurationPropertyRepository getRepository() {
        if (repository == null) {
            repository = this.beanFactory.getBean(ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        }
        return repository;
    }

}
