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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static io.microsphere.text.FormatUtils.format;
import static java.lang.Integer.parseInt;

/**
 * A repository for managing {@link ConfigurationProperty} instances with support for configuration via Spring's {@link Environment}.
 * <p>
 * This class provides methods to add, remove, retrieve, and manage configuration properties. It also allows integration with Spring's
 * lifecycle management through the {@link InitializingBean} and {@link DisposableBean} interfaces, as well as environment-based
 * property configuration through the {@link EnvironmentAware} interface.
 * </p>
 *
 * <h3>Configuration Properties</h3>
 * <ul>
 *     <li>{@value #MAX_SIZE_PROPERTY_NAME}: Sets the maximum number of properties that can be stored in the repository.
 *         Defaults to {@value #DEFAULT_MAX_SIZE} if not specified.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a new repository instance
 * ConfigurationPropertyRepository repository = new ConfigurationPropertyRepository();
 *
 * // Set the environment to load max size from configuration
 * ConfigurableEnvironment environment = new StandardEnvironment();
 * repository.setEnvironment(environment);
 *
 * // Initialize the repository (usually done automatically by Spring)
 * repository.afterPropertiesSet();
 *
 * // Add a configuration property
 * ConfigurationProperty property = new ConfigurationProperty("my.property.name");
 * property.setValue("exampleValue");
 * repository.add(property);
 *
 * // Retrieve the property
 * ConfigurationProperty retrieved = repository.get("my.property.name");
 * System.out.println(retrieved.getValue()); // Output: exampleValue
 *
 * // Clean up resources when done
 * repository.destroy();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class ConfigurationPropertyRepository implements EnvironmentAware, InitializingBean, DisposableBean {

    public static final String BEAN_NAME = "configurationPropertyRepository";

    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "config-property-repository.";

    private static final String DEFAULT_MAX_SIZE = "99999";

    /**
     * The default max size of {@link ConfigurationPropertyRepository}
     */
    public static final int DEFAULT_MAX_SIZE_PROPERTY_VALUE = parseInt(DEFAULT_MAX_SIZE);

    /**
     * The max size of {@link ConfigurationPropertyRepository}
     */
    @io.microsphere.annotation.ConfigurationProperty(
            type = int.class,
            description = "The max size of the repository for ConfigurationProperty instances",
            defaultValue = DEFAULT_MAX_SIZE
    )
    public static final String MAX_SIZE_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "max-size";

    private int maxSize = DEFAULT_MAX_SIZE_PROPERTY_VALUE;

    private ConcurrentMap<String, ConfigurationProperty> repository;

    /**
     * Add a {@link ConfigurationProperty} instance
     *
     * @param configurationProperty a {@link ConfigurationProperty} instance
     */
    public void add(ConfigurationProperty configurationProperty) {
        assertMaxSize();
        String name = configurationProperty.getName();
        Map<String, ConfigurationProperty> repository = getRepository();
        repository.put(name, configurationProperty);
    }

    /**
     * Remove a {@link ConfigurationProperty} instance by name
     *
     * @param name {@link ConfigurationProperty#getName() the name of ConfigurationProperty}
     */
    public ConfigurationProperty remove(String name) {
        Map<String, ConfigurationProperty> repository = getRepository();
        return repository.remove(name);
    }

    /**
     * Get a {@link ConfigurationProperty} instance by name
     *
     * @param name {@link ConfigurationProperty#getName() the name of ConfigurationProperty}
     * @return <code>null</code> if not found
     */
    public ConfigurationProperty get(String name) {
        Map<String, ConfigurationProperty> repository = getRepository();
        return repository.get(name);
    }

    /**
     * Determine whether the repository contains the specified name
     *
     * @param name {@link ConfigurationProperty#getName() the name of ConfigurationProperty}
     * @return <code>true</code> if contains, otherwise <code>false</code>
     */
    public boolean contains(String name) {
        Map<String, ConfigurationProperty> repository = getRepository();
        return repository.containsKey(name);
    }

    /**
     * Create a {@link ConfigurationProperty} instance if absent
     *
     * @param name {@link ConfigurationProperty#getName() the name of ConfigurationProperty}
     * @return the {@link ConfigurationProperty} instance
     */
    public ConfigurationProperty createIfAbsent(String name) {
        assertMaxSize();
        Map<String, ConfigurationProperty> repository = getRepository();
        return repository.computeIfAbsent(name, ConfigurationProperty::new);
    }

    /**
     * Get all {@link ConfigurationProperty} instances
     *
     * @return never <code>null</code>
     */
    public Collection<ConfigurationProperty> getAll() {
        Map<String, ConfigurationProperty> repository = getRepository();
        return repository.values();
    }

    /**
     * Get the max size of the repository
     *
     * @return max size
     */
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.maxSize = environment.getProperty(MAX_SIZE_PROPERTY_NAME, int.class, DEFAULT_MAX_SIZE_PROPERTY_VALUE);
    }

    @Override
    public void afterPropertiesSet() {
        this.repository = newConcurrentHashMap(this.maxSize);
    }

    /**
     * clear the repository
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        if (repository != null) {
            repository.clear();
        }
    }

    protected Map<String, ConfigurationProperty> getRepository() {
        if (repository == null) {
            this.afterPropertiesSet();
        }
        return repository;
    }

    private void assertMaxSize() {
        if (repository != null && repository.size() >= maxSize) {
            String message = format("The size of repository is greater than max size : {}. " +
                    "If it requires to the greater threshold, change the configuration property : {}", maxSize, MAX_SIZE_PROPERTY_NAME);
            throw new IllegalStateException(message);
        }
    }
}
