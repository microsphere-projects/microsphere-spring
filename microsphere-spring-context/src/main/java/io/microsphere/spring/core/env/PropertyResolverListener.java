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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either exbeforess or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.core.env;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.PropertyResolver;

import javax.annotation.Nullable;

/**
 * The interface listens the manipulation of {@link ConfigurablePropertyResolver PropertyResolvers'} profiles including:
 *
 * <ul>
 *     <li>{@link PropertyResolver#getProperty(String, String)}</li>
 *     <li>{@link PropertyResolver#getProperty(String, Class, Object)}</li>
 *     <li>{@link PropertyResolver#getRequiredProperty(String, Class)}</li>
 *     <li>{@link PropertyResolver#resolvePlaceholders(String)}</li>
 *     <li>{@link PropertyResolver#resolveRequiredPlaceholders(String)}</li>
 *     <li>{@link ConfigurablePropertyResolver#setRequiredProperties(String...)}</li>
 *     <li>{@link ConfigurablePropertyResolver#validateRequiredProperties()}</li>
 *     <li>{@link ConfigurablePropertyResolver#setConversionService(ConfigurableConversionService)}</li>
 *     <li>{@link ConfigurablePropertyResolver#setPlaceholderPrefix(String)}</li>
 *     <li>{@link ConfigurablePropertyResolver#setPlaceholderSuffix(String)}</li>
 *     <li>{@link ConfigurablePropertyResolver#setIgnoreUnresolvableNestedPlaceholders(boolean)}</li>
 *     <li>{@link ConfigurablePropertyResolver#setValueSeparator(String)}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurablePropertyResolver
 * @since 1.0.0
 */
public interface PropertyResolverListener {

    /**
     * Callback before {@link PropertyResolver#getProperty(String, String)} or{@link PropertyResolver#getProperty(String, Class, Object)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param name             the name of the property
     * @param targetType       the target type to be converted by the property
     * @param defaultValue     the default value of the target-typed property
     */
    default void beforeGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                   @Nullable Object defaultValue) {
    }

    /**
     * Callback after {@link PropertyResolver#getProperty(String, String)} or {@link PropertyResolver#getProperty(String, Class, Object)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param name             the name of the property
     * @param value            the value of property
     * @param targetType       the target type to be converted by the property
     * @param defaultValue     the default value of the target-typed property
     */
    default void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType,
                                  @Nullable Object value, @Nullable Object defaultValue) {
    }

    /**
     * Callback before {@link PropertyResolver#getRequiredProperty(String)} or {@link PropertyResolver#getRequiredProperty(String, Class)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param name             the name of the property
     * @param targetType       the target type to be converted by the property
     */
    default void beforeGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType) {
    }

    /**
     * Callback after {@link PropertyResolver#getRequiredProperty(String)} or {@link PropertyResolver#getRequiredProperty(String, Class)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param name             the name of the property
     * @param targetType       the target type to be converted by the property
     * @param value            the value of the target-typed property
     */
    default void afterGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object value) {
    }

    /**
     * Callback before {@link PropertyResolver#resolvePlaceholders(String)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param text             the text to be resolved
     */
    default void beforeResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {
    }

    /**
     * Callback after {@link PropertyResolver#resolvePlaceholders(String)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param text             the text to be resolved
     * @param result           the resolved result
     */
    default void afterResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#resolveRequiredPlaceholders(String)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param text             the text to be resolved
     */
    default void beforeResolveRequiredPlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {
    }

    /**
     * Callback after {@link PropertyResolver#resolveRequiredPlaceholders(String)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param text             the text to be resolved
     * @param result           the resolved result
     */
    default void afterResolveRequiredPlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#setRequiredProperties(String...)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param properties       the required properties
     */
    default void beforeSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#setRequiredProperties(String...)}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param properties       the required properties
     */
    default void afterSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#validateRequiredProperties()}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     */
    default void beforeValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#validateRequiredProperties()}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     */
    default void afterValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#getConversionService()}.
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     */
    default void beforeGetConversionService(ConfigurablePropertyResolver propertyResolver) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#getConversionService()}.
     *
     * @param propertyResolver  {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param conversionService {@link ConfigurableConversionService}
     */
    default void afterGetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#setConversionService(ConfigurableConversionService)}.
     *
     * @param propertyResolver  {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param conversionService {@link ConfigurableConversionService}
     */
    default void beforeSetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#setConversionService(ConfigurableConversionService)}
     *
     * @param propertyResolver  {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param conversionService {@link ConfigurableConversionService}
     */
    default void afterSetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#setPlaceholderPrefix(String)}
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param prefix           the placeholder prefix
     */
    default void beforeSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#setPlaceholderPrefix(String)}
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param prefix           the placeholder prefix
     */
    default void afterSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#setPlaceholderSuffix(String)}
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param suffix           the placeholder suffix
     */
    default void beforeSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#setPlaceholderSuffix(String)}
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param suffix           the placeholder suffix
     */
    default void afterSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#setIgnoreUnresolvableNestedPlaceholders(boolean)}
     *
     * @param propertyResolver                     {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param ignoreUnresolvableNestedPlaceholders Set whether to throw an exception when encountering an unresolvable placeholder nested within the value of a given property.
     */
    default void beforeSetIgnoreUnresolvableNestedPlaceholders(ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#setIgnoreUnresolvableNestedPlaceholders(boolean)}
     *
     * @param propertyResolver                     {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param ignoreUnresolvableNestedPlaceholders Set whether to throw an exception when encountering an unresolvable placeholder nested within the value of a given property.
     */
    default void afterSetIgnoreUnresolvableNestedPlaceholders(ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {
    }

    /**
     * Callback before {@link ConfigurablePropertyResolver#setValueSeparator(String)}
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param valueSeparator   the separating character between the placeholders replaced by this resolver and their associated default value
     */
    default void beforeSetValueSeparator(ConfigurablePropertyResolver propertyResolver, @Nullable String valueSeparator) {
    }

    /**
     * Callback after {@link ConfigurablePropertyResolver#setValueSeparator(String)}
     *
     * @param propertyResolver {@link ConfigurablePropertyResolver the underlying ConfigurablePropertyResolver}
     * @param valueSeparator   the separating character between the placeholders replaced by this resolver and their associated default value
     */
    default void afterSetValueSeparator(ConfigurablePropertyResolver propertyResolver, @Nullable String valueSeparator) {
    }

}
