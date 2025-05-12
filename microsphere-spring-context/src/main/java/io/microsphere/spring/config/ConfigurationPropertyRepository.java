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

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static io.microsphere.text.FormatUtils.format;

/**
 * {@link ConfigurationProperty} Repository
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public class ConfigurationPropertyRepository implements EnvironmentAware, InitializingBean, DisposableBean {

    public static final String BEAN_NAME = "configurationPropertyRepository";

    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "config-property-repository.";

    public static final String MAX_SIZE_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "max-size";

    public static final int DEFAULT_MAX_SIZE_PROPERTY_VALUE = 99999;

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
        this.repository = new ConcurrentHashMap<>(this.maxSize);
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
