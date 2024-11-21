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
package io.microsphere.spring.core.env;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.spring.util.SpringFactoriesLoaderUtils.loadFactories;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * {@link ConfigurableEnvironment} with intercepting features
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurableEnvironment
 * @see ListenableConfigurableEnvironmentInitializer
 * @since 1.0.0
 */
public class ListenableConfigurableEnvironment implements ConfigurableEnvironment {

    private final ConfigurableEnvironment delegate;

    private List<EnvironmentListener> environmentListeners;

    private final List<ProfileListener> profileListeners;

    private final List<PropertyResolverListener> propertyResolverListeners;

    public ListenableConfigurableEnvironment(ConfigurableApplicationContext applicationContext) {
        this.delegate = applicationContext.getEnvironment();
        List<EnvironmentListener> environmentListeners = loadEnvironmentListeners(applicationContext);
        this.environmentListeners = environmentListeners;
        this.profileListeners = loadProfileListeners(applicationContext, environmentListeners);
        this.propertyResolverListeners = loadPropertyResolverListeners(applicationContext, environmentListeners);
    }

    private static List<EnvironmentListener> loadEnvironmentListeners(ConfigurableApplicationContext applicationContext) {
        return loadFactories(applicationContext, EnvironmentListener.class);
    }

    private List<ProfileListener> loadProfileListeners(ConfigurableApplicationContext applicationContext,
                                                       List<EnvironmentListener> environmentListeners) {
        // Add all EnvironmentListener instances
        List<ProfileListener> profileListeners = new LinkedList<>(environmentListeners);
        // Load Spring Factories with extension
        profileListeners.addAll(loadFactories(applicationContext, ProfileListener.class));
        // Sort
        sort(profileListeners);
        return profileListeners;
    }

    private List<PropertyResolverListener> loadPropertyResolverListeners(ConfigurableApplicationContext applicationContext,
                                                                         List<EnvironmentListener> environmentListeners) {
        // Add all EnvironmentListener instances
        List<PropertyResolverListener> propertyResolverListeners = new LinkedList<>(environmentListeners);
        // Load Spring Factories with extension
        propertyResolverListeners.addAll(loadFactories(applicationContext, PropertyResolverListener.class));
        // Sort
        sort(propertyResolverListeners);
        return propertyResolverListeners;
    }

    @Override
    public void setActiveProfiles(String... profiles) {
        forEachProfileListener(listener -> listener.beforeSetActiveProfiles(delegate, profiles));
        delegate.setActiveProfiles(profiles);
        forEachProfileListener(listener -> listener.afterSetActiveProfiles(delegate, profiles));
    }

    @Override
    public void addActiveProfile(String profile) {
        forEachProfileListener(listener -> listener.beforeAddActiveProfile(delegate, profile));
        delegate.addActiveProfile(profile);
        forEachProfileListener(listener -> listener.afterAddActiveProfile(delegate, profile));
    }

    @Override
    public void setDefaultProfiles(String... profiles) {
        forEachProfileListener(listener -> listener.beforeSetDefaultProfiles(delegate, profiles));
        delegate.setDefaultProfiles(profiles);
        forEachProfileListener(listener -> listener.afterSetDefaultProfiles(delegate, profiles));
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return delegate.getPropertySources();
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        return delegate.getSystemProperties();
    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        return delegate.getSystemEnvironment();
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {
        forEachEnvironmentListener(listener -> listener.beforeMerge(delegate, parent));
        delegate.merge(parent);
        forEachEnvironmentListener(listener -> listener.afterMerge(delegate, parent));
    }

    @Override
    public String[] getActiveProfiles() {
        return delegate.getActiveProfiles();
    }

    @Override
    public String[] getDefaultProfiles() {
        return delegate.getDefaultProfiles();
    }

    @Override
    public boolean matchesProfiles(String... profileExpressions) {
        return delegate.matchesProfiles(profileExpressions);
    }

    @Deprecated
    @Override
    public boolean acceptsProfiles(String... profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    @Override
    public boolean acceptsProfiles(Profiles profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    @Override
    public boolean containsProperty(String key) {
        return delegate.containsProperty(key);
    }

    @Nullable
    @Override
    public String getProperty(String key) {
        forEachPropertyResolverListener(listener -> listener.beforeGetProperty(delegate, key, null));
        String value = delegate.getProperty(key);
        forEachPropertyResolverListener(listener -> listener.afterGetProperty(delegate, key, value, null));
        return value;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return delegate.getProperty(key, defaultValue);
    }

    @Nullable
    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return delegate.getProperty(key, targetType);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return delegate.getProperty(key, targetType, defaultValue);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return delegate.getRequiredProperty(key);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return delegate.getRequiredProperty(key, targetType);
    }

    @Override
    public String resolvePlaceholders(String text) {
        return delegate.resolvePlaceholders(text);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return delegate.resolveRequiredPlaceholders(text);
    }

    @Override
    public ConfigurableConversionService getConversionService() {
        return delegate.getConversionService();
    }

    @Override
    public void setConversionService(ConfigurableConversionService conversionService) {
        delegate.setConversionService(conversionService);
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        delegate.setPlaceholderPrefix(placeholderPrefix);
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        delegate.setPlaceholderSuffix(placeholderSuffix);
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        delegate.setValueSeparator(valueSeparator);
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        delegate.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        delegate.setRequiredProperties(requiredProperties);
    }

    @Override
    public void validateRequiredProperties() throws MissingRequiredPropertiesException {
        delegate.validateRequiredProperties();
    }

    private void forEachEnvironmentListener(Consumer<EnvironmentListener> listenerConsumer) {
        forEach(this.environmentListeners, listenerConsumer);
    }

    private void forEachProfileListener(Consumer<ProfileListener> listenerConsumer) {
        forEach(this.profileListeners, listenerConsumer);
    }

    private void forEachPropertyResolverListener(Consumer<PropertyResolverListener> listenerConsumer) {
        forEach(this.propertyResolverListeners, listenerConsumer);
    }
}
