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

    /**
     * Invoked after a property is retrieved from the {@link ConfigurablePropertyResolver}.
     * Records the property's target type, resolved value, and default value in the
     * {@link ConfigurationPropertyRepository}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Triggered automatically when resolving a property:
     *   String value = environment.getProperty("test-name");
     *   // After resolution, the listener records the property details:
     *   ConfigurationProperty property = repository.get("test-name");
     *   assertEquals("test-value", property.getValue());
     *   assertEquals(String.class.getName(), property.getType());
     * }</pre>
     *
     * @param propertyResolver the {@link ConfigurablePropertyResolver} that resolved the property
     * @param name             the property name
     * @param targetType       the target type of the property value
     * @param value            the resolved property value
     * @param defaultValue     the default value if the property is not found
     */
    @Override
    public void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                 Object value, Object defaultValue) {
        ConfigurationProperty configurationProperty = getConfigurationProperty(name);
        configurationProperty.setType(targetType);
        configurationProperty.setValue(value);
        configurationProperty.setDefaultValue(defaultValue);
    }

    /**
     * Invoked after a required property is retrieved from the {@link ConfigurablePropertyResolver}.
     * Records the property's target type and resolved value, and marks it as required in the
     * {@link ConfigurationPropertyRepository}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Triggered automatically when resolving a required property:
     *   String value = environment.getRequiredProperty("test-name");
     *   // After resolution, the listener records the property as required:
     *   ConfigurationProperty property = repository.get("test-name");
     *   assertEquals("test-value", property.getValue());
     *   assertTrue(property.isRequired());
     * }</pre>
     *
     * @param propertyResolver the {@link ConfigurablePropertyResolver} that resolved the property
     * @param name             the property name
     * @param targetType       the target type of the property value
     * @param value            the resolved property value
     */
    @Override
    public void afterGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                         Object value) {
        ConfigurationProperty configurationProperty = getConfigurationProperty(name);
        configurationProperty.setType(targetType);
        configurationProperty.setValue(value);
        configurationProperty.setRequired(true);
    }

    /**
     * Invoked after the placeholder prefix is changed on the {@link ConfigurablePropertyResolver}.
     * Updates the internally tracked placeholder prefix.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Change the placeholder prefix on the environment:
     *   environment.setPlaceholderPrefix("#(");
     *   // The listener tracks the updated prefix:
     *   assertEquals("#(", listener.getPlaceholderPrefix());
     * }</pre>
     *
     * @param propertyResolver the {@link ConfigurablePropertyResolver} whose prefix was changed
     * @param prefix           the new placeholder prefix
     */
    @Override
    public void afterSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
        this.placeholderPrefix = prefix;
    }

    /**
     * Invoked after the placeholder suffix is changed on the {@link ConfigurablePropertyResolver}.
     * Updates the internally tracked placeholder suffix.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Change the placeholder suffix on the environment:
     *   environment.setPlaceholderSuffix(")");
     *   // The listener tracks the updated suffix:
     *   assertEquals(")", listener.getPlaceholderSuffix());
     * }</pre>
     *
     * @param propertyResolver the {@link ConfigurablePropertyResolver} whose suffix was changed
     * @param suffix           the new placeholder suffix
     */
    @Override
    public void afterSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
        this.placeholderSuffix = suffix;
    }

    /**
     * Sets the {@link BeanFactory} and registers the {@link ConfigurationPropertyRepository} bean definition
     * as well as this listener as a named bean in the {@link BeanDefinitionRegistry}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked automatically by Spring during context initialization:
     *   CollectingConfigurationPropertyListener listener = new CollectingConfigurationPropertyListener();
     *   listener.setBeanFactory(beanFactory);
     *   // After setBeanFactory, the ConfigurationPropertyRepository is registered:
     *   assertTrue(beanFactory.containsBean(ConfigurationPropertyRepository.BEAN_NAME));
     * }</pre>
     *
     * @param beanFactory the owning {@link BeanFactory}
     * @throws BeansException if registration fails
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
        registerBeanDefinition(registry, ConfigurationPropertyRepository.BEAN_NAME, ConfigurationPropertyRepository.class);
        registerBean(registry, BEAN_NAME, this);
    }

    /**
     * Returns the current placeholder prefix used for property resolution.
     * Defaults to {@code "${"} unless changed via
     * {@link #afterSetPlaceholderPrefix(ConfigurablePropertyResolver, String)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   CollectingConfigurationPropertyListener listener = ...; // obtained from Spring context
     *   String prefix = listener.getPlaceholderPrefix();
     *   assertEquals("${", prefix);
     * }</pre>
     *
     * @return the current placeholder prefix
     */
    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }

    /**
     * Returns the current placeholder suffix used for property resolution.
     * Defaults to {@code "}"} unless changed via
     * {@link #afterSetPlaceholderSuffix(ConfigurablePropertyResolver, String)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   CollectingConfigurationPropertyListener listener = ...; // obtained from Spring context
     *   String suffix = listener.getPlaceholderSuffix();
     *   assertEquals("}", suffix);
     * }</pre>
     *
     * @return the current placeholder suffix
     */
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
