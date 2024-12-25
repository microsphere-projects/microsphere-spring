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

import io.microsphere.constants.PropertyConstants;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
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

    private final static Logger logger = LoggerFactory.getLogger(ListenableConfigurableEnvironment.class);

    /**
     * The prefix of the property name of {@link ListenableConfigurableEnvironment}
     */
    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-environment.";

    /**
     * The property name of {@link ListenableConfigurableEnvironment} to be 'enabled'
     */
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The default property value of {@link ListenableConfigurableEnvironment} to be 'enabled'
     */
    public static final boolean ENABLED_PROPERTY_VALUE = false;

    /**
     * The {@link MethodHandle} of {@link Environment#acceptsProfiles(Profiles)} since Spring Framework 5.1
     */
    @Nullable
    private static final MethodHandle acceptsProfilesMethodHandle = findVirtual(Environment.class, "acceptsProfiles", Profiles.class);

    /**
     * The {@link MethodHandle} of {@link Environment#matchesProfiles(String...)} since Spring Framework 5.3.8
     */
    @Nullable
    private static final MethodHandle matchesProfilesMethodHandle = findVirtual(Environment.class, "matchesProfiles", String[].class);

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
        forEachEnvironmentListener(listener -> listener.beforeGetPropertySources(delegate));
        MutablePropertySources propertySources = delegate.getPropertySources();
        forEachEnvironmentListener(listener -> listener.afterGetPropertySources(delegate, propertySources));
        return propertySources;
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        forEachEnvironmentListener(listener -> listener.beforeGetSystemProperties(delegate));
        Map<String, Object> systemProperties = delegate.getSystemProperties();
        forEachEnvironmentListener(listener -> listener.afterGetSystemProperties(delegate, systemProperties));
        return systemProperties;
    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        forEachEnvironmentListener(listener -> listener.beforeGetSystemEnvironment(delegate));
        Map<String, Object> systemEnvironment = delegate.getSystemEnvironment();
        forEachEnvironmentListener(listener -> listener.afterGetSystemEnvironment(delegate, systemEnvironment));
        return systemEnvironment;
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {
        forEachEnvironmentListener(listener -> listener.beforeMerge(delegate, parent));
        delegate.merge(parent);
        forEachEnvironmentListener(listener -> listener.afterMerge(delegate, parent));
    }

    @Override
    public String[] getActiveProfiles() {
        forEachProfileListener(listener -> listener.beforeGetActiveProfiles(delegate));
        String[] activeProfiles = delegate.getActiveProfiles();
        forEachProfileListener(listener -> listener.afterGetActiveProfiles(delegate, activeProfiles));
        return activeProfiles;
    }

    @Override
    public String[] getDefaultProfiles() {
        forEachProfileListener(listener -> listener.beforeGetDefaultProfiles(delegate));
        String[] defaultProfiles = delegate.getDefaultProfiles();
        forEachProfileListener(listener -> listener.afterGetDefaultProfiles(delegate, defaultProfiles));
        return defaultProfiles;
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.3.28
     */
    public boolean matchesProfiles(String... profileExpressions) {
        if (matchesProfilesMethodHandle != null) {
            try {
                return (boolean) matchesProfilesMethodHandle.invokeExact((Environment) delegate, profileExpressions);
            } catch (Throwable e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to invoke {} with args : '{}'", matchesProfilesMethodHandle, Arrays.toString(profileExpressions), e);
                }
            }
        }
        return acceptsProfiles(profileExpressions);
    }

    @Deprecated
    @Override
    public boolean acceptsProfiles(String... profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 5.1
     */
    public boolean acceptsProfiles(Profiles profiles) {
        if (acceptsProfilesMethodHandle != null) {
            try {
                return (boolean) acceptsProfilesMethodHandle.invokeExact((Environment) delegate, profiles);
            } catch (Throwable e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to invoke {} with args : '{}'", acceptsProfilesMethodHandle, profiles, e);
                }
            }
        }
        throw new UnsupportedOperationException("The method acceptsProfiles(Profiles) is not supported before Spring Framework 5.1");
    }

    @Override
    public boolean containsProperty(String key) {
        return delegate.containsProperty(key);
    }

    @Nullable
    @Override
    public String getProperty(String key) {
        forEachPropertyResolverListener(listener -> listener.beforeGetProperty(delegate, key, String.class, null));
        String value = delegate.getProperty(key);
        forEachPropertyResolverListener(listener -> listener.afterGetProperty(delegate, key, String.class, value, null));
        return value;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        forEachPropertyResolverListener(listener -> listener.beforeGetProperty(delegate, key, String.class, defaultValue));
        String value = delegate.getProperty(key, defaultValue);
        forEachPropertyResolverListener(listener -> listener.afterGetProperty(delegate, key, String.class, value, defaultValue));
        return value;
    }

    @Nullable
    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        forEachPropertyResolverListener(listener -> listener.beforeGetProperty(delegate, key, targetType, null));
        T value = delegate.getProperty(key, targetType);
        forEachPropertyResolverListener(listener -> listener.afterGetProperty(delegate, key, targetType, value, null));
        return value;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        forEachPropertyResolverListener(listener -> listener.beforeGetProperty(delegate, key, targetType, defaultValue));
        T value = delegate.getProperty(key, targetType, defaultValue);
        forEachPropertyResolverListener(listener -> listener.afterGetProperty(delegate, key, targetType, value, defaultValue));
        return value;
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        forEachPropertyResolverListener(listener -> listener.beforeGetRequiredProperty(delegate, key, String.class));
        String value = delegate.getRequiredProperty(key);
        forEachPropertyResolverListener(listener -> listener.afterGetRequiredProperty(delegate, key, String.class, value));
        return value;
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        forEachPropertyResolverListener(listener -> listener.beforeGetRequiredProperty(delegate, key, targetType));
        T value = delegate.getRequiredProperty(key, targetType);
        forEachPropertyResolverListener(listener -> listener.afterGetRequiredProperty(delegate, key, targetType, value));
        return value;
    }

    @Override
    public String resolvePlaceholders(String text) {
        forEachPropertyResolverListener(listener -> listener.beforeResolvePlaceholders(delegate, text));
        String result = delegate.resolvePlaceholders(text);
        forEachPropertyResolverListener(listener -> listener.afterResolvePlaceholders(delegate, text, result));
        return result;
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        forEachPropertyResolverListener(listener -> listener.beforeResolveRequiredPlaceholders(delegate, text));
        String result = delegate.resolveRequiredPlaceholders(text);
        forEachPropertyResolverListener(listener -> listener.afterResolveRequiredPlaceholders(delegate, text, result));
        return result;
    }

    @Override
    public ConfigurableConversionService getConversionService() {
        forEachPropertyResolverListener(listener -> listener.beforeGetConversionService(delegate));
        ConfigurableConversionService conversionService = delegate.getConversionService();
        forEachPropertyResolverListener(listener -> listener.afterGetConversionService(delegate, conversionService));
        return conversionService;
    }

    @Override
    public void setConversionService(ConfigurableConversionService conversionService) {
        forEachPropertyResolverListener(listener -> listener.beforeSetConversionService(delegate, conversionService));
        delegate.setConversionService(conversionService);
        forEachPropertyResolverListener(listener -> listener.afterSetConversionService(delegate, conversionService));
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        forEachEnvironmentListener(listener -> listener.beforeSetPlaceholderPrefix(delegate, placeholderPrefix));
        delegate.setPlaceholderPrefix(placeholderPrefix);
        forEachEnvironmentListener(listener -> listener.afterSetPlaceholderPrefix(delegate, placeholderPrefix));
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        forEachEnvironmentListener(listener -> listener.beforeSetPlaceholderSuffix(delegate, placeholderSuffix));
        delegate.setPlaceholderSuffix(placeholderSuffix);
        forEachEnvironmentListener(listener -> listener.afterSetPlaceholderSuffix(delegate, placeholderSuffix));
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        forEachEnvironmentListener(listener -> listener.beforeSetValueSeparator(delegate, valueSeparator));
        delegate.setValueSeparator(valueSeparator);
        forEachEnvironmentListener(listener -> listener.afterSetValueSeparator(delegate, valueSeparator));
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        forEachEnvironmentListener(listener -> listener.beforeSetIgnoreUnresolvableNestedPlaceholders(delegate, ignoreUnresolvableNestedPlaceholders));
        delegate.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
        forEachEnvironmentListener(listener -> listener.afterSetIgnoreUnresolvableNestedPlaceholders(delegate, ignoreUnresolvableNestedPlaceholders));
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        forEachEnvironmentListener(listener -> listener.beforeSetRequiredProperties(delegate, requiredProperties));
        delegate.setRequiredProperties(requiredProperties);
        forEachEnvironmentListener(listener -> listener.afterSetRequiredProperties(delegate, requiredProperties));
    }

    @Override
    public void validateRequiredProperties() throws MissingRequiredPropertiesException {
        forEachEnvironmentListener(listener -> listener.beforeValidateRequiredProperties(delegate));
        delegate.validateRequiredProperties();
        forEachEnvironmentListener(listener -> listener.afterValidateRequiredProperties(delegate));
    }

    /**
     * Return {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     *
     * @return {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     */
    @Nonnull
    public ConfigurableEnvironment getDelegate() {
        return this.delegate;
    }

    /**
     * Set the {@link ListenableConfigurableEnvironment} into {@link ConfigurableApplicationContext} if
     * {@link #isEnabled(Environment) enabled}
     *
     * @param applicationContext {@link ConfigurableApplicationContext}
     */
    public static void setEnvironmentIfEnabled(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        if (!isEnabled(environment) && environment instanceof ListenableConfigurableEnvironment) {
            return;
        }
        applicationContext.setEnvironment(new ListenableConfigurableEnvironment(applicationContext));
    }

    /**
     * Determine whether the {@link ListenableConfigurableEnvironment} is enabled
     *
     * @param environment {@link Environment the underlying Environment}
     * @return <code>true</code> if enabled, <code>false</code> otherwise
     */
    public static boolean isEnabled(Environment environment) {
        return environment.getProperty(ENABLED_PROPERTY_NAME, boolean.class, ENABLED_PROPERTY_VALUE);
    }

    private void forEachEnvironmentListener(Consumer<EnvironmentListener> listenerConsumer) {
        forEachListener(this.environmentListeners, listenerConsumer);
    }

    private void forEachProfileListener(Consumer<ProfileListener> listenerConsumer) {
        forEachListener(this.profileListeners, listenerConsumer);
    }

    private void forEachPropertyResolverListener(Consumer<PropertyResolverListener> listenerConsumer) {
        forEachListener(this.propertyResolverListeners, listenerConsumer);
    }

    private <T> void forEachListener(List<T> listeners, Consumer<T> consumer) {
        int size = listeners.size();
        if (size < 1) {
            return;
        }
        for (int i = 0; i < size; i++) {
            T listener = listeners.get(i);
            try {
                consumer.accept(listener);
            } catch (Throwable e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Listener(type : '{}' , index : {}) execution is failed!", listener.getClass().getTypeName(), i, e);
                }
            }
        }
    }
}
