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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import static io.microsphere.spring.util.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.util.BeanRegistrar.registerBean;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.util.StringUtils.substringBetween;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_PREFIX;
import static org.springframework.util.SystemPropertyUtils.PLACEHOLDER_SUFFIX;
import static org.springframework.util.SystemPropertyUtils.VALUE_SEPARATOR;

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
        BeanFactoryAware, EnvironmentAware {

    public static final String BEAN_NAME = "collectingConfigurationPropertyListener";

    private BeanFactory beanFactory;

    private ConfigurableEnvironment environment;

    private ConversionService conversionService;

    private ConfigurationPropertyRepository repository;

    private ConfigurablePropertyResolver propertyResolver;

    private String placeholderPrefix = PLACEHOLDER_PREFIX;

    private String placeholderSuffix = PLACEHOLDER_SUFFIX;

    private String valueSeparator = VALUE_SEPARATOR;


    @Override
    public void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                 Object value, Object defaultValue) {
        this.propertyResolver = propertyResolver;

        ConfigurationProperty configurationProperty = getConfigurationProperty(name);
        configurationProperty.setType(targetType);
        configurationProperty.setValue(value);
        configurationProperty.setDefaultValue(defaultValue);

        // TODO Add Property target for ConfigurationProperty
        addTarget(configurationProperty, "Property");
    }

    @Override
    public void afterGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                         Object value) {
        ConfigurationProperty configurationProperty = getConfigurationProperty(name);
        configurationProperty.setType(targetType);
        configurationProperty.setValue(value);
        configurationProperty.setRequired(true);
        // TODO Add Required Property target for ConfigurationProperty
        addTarget(configurationProperty, "Required Property");
    }

    @Override
    public void afterResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
        ConfigurationProperty configurationProperty = resolveConfigurationProperty(text, String.class, result);
        // TODO Add Placeholders target for ConfigurationProperty
        addTarget(configurationProperty, "Resolve Placeholders");
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
    public void afterSetValueSeparator(ConfigurablePropertyResolver propertyResolver, String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        Value valueAnnotation = descriptor.getAnnotation(Value.class);
        // @Value("${user.name}")
        if (valueAnnotation != null && suggestedValue instanceof String) {
            String expression = (String) suggestedValue;
            Class<?> targetType = descriptor.getDependencyType();
            ConfigurationProperty configurationProperty = resolveConfigurationProperty(expression, targetType);
            // TODO Add @Value target for ConfigurationProperty
            addTarget(configurationProperty, "@Value Injection");
        }
    }

    private ConfigurationProperty resolveConfigurationProperty(String expression, Class<?> targetType) {
        return this.resolveConfigurationProperty(expression, targetType, null);
    }

    private <T> ConfigurationProperty resolveConfigurationProperty(String expression, Class<T> targetType,
                                                                   @Nullable T propertyValue) {
        String propertyName = substringBetween(expression, this.placeholderPrefix, this.placeholderSuffix);
        if (propertyName == null) { // Maybe @Value("#{systemProperties.myProp}")
            return null;
        }
        String defaultValue = null;
        // Consider the property name containing the default value like : @Value("${user.name:unnamed}")
        int indexOfVS = propertyName.indexOf(this.valueSeparator);
        if (indexOfVS > 0) {
            defaultValue = propertyName.substring(indexOfVS + 1);
            propertyName = propertyName.substring(0, indexOfVS);
        }
        boolean required = defaultValue == null;

        T targetDefaultValue = required ? null : this.conversionService.convert(defaultValue, targetType);

        propertyValue = propertyValue == null ? getProperty(propertyName, targetType, targetDefaultValue) : propertyValue;

        ConfigurationProperty configurationProperty = getConfigurationProperty(propertyName);
        configurationProperty.setType(targetType);
        configurationProperty.setValue(propertyValue);
        configurationProperty.setRequired(required);
        return configurationProperty;
    }

    private <T> T getProperty(String name, Class<T> targetType, @Nullable T defaultValue) {
        return defaultValue == null ? this.environment.getRequiredProperty(name, targetType)
                : this.environment.getProperty(name, targetType, defaultValue);
    }

    private void addTarget(ConfigurationProperty configurationProperty, String target) {
        ConfigurationProperty.Metadata metadata = configurationProperty.getMetadata();
        metadata.getTargets().add(target);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
        registerBeanDefinition(registry, ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        registerBean(registry, BEAN_NAME, this);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
        this.conversionService = this.environment.getConversionService();
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
