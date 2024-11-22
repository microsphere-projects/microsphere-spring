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

import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Map;

/**
 * {@link PropertyResolverListener} class for logging
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class LoggingEnvironmentListener implements EnvironmentListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingEnvironmentListener.class);

    @Override
    public void beforeGetPropertySources(ConfigurableEnvironment environment) {
        log("beforeGetPropertySources()");
    }

    @Override
    public void afterGetPropertySources(ConfigurableEnvironment environment, MutablePropertySources propertySources) {
        log("afterGetPropertySources() : {}", propertySources);
    }

    @Override
    public void beforeGetSystemProperties(ConfigurableEnvironment environment) {
        log("beforeGetSystemProperties()");
    }

    @Override
    public void afterGetSystemProperties(ConfigurableEnvironment environment, Map<String, Object> systemProperties) {
        log("afterGetSystemProperties() : {}", systemProperties);
    }

    @Override
    public void beforeGetSystemEnvironment(ConfigurableEnvironment environment) {
        log("beforeGetSystemEnvironment()");
    }

    @Override
    public void afterGetSystemEnvironment(ConfigurableEnvironment environment, Map<String, Object> systemEnvironmentVariables) {
        log("afterGetSystemEnvironment() : {}", systemEnvironmentVariables);
    }

    @Override
    public void beforeMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
        log("beforeMerge(parentEnvironment : {})", parentEnvironment);
    }

    @Override
    public void afterMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
        log("afterMerge(parentEnvironment : {})", parentEnvironment);
    }

    @Override
    public void beforeGetActiveProfiles(Environment environment) {
        log("beforeGetActiveProfiles()");
    }

    @Override
    public void afterGetActiveProfiles(Environment environment, String[] activeProfiles) {
        log("afterGetActiveProfiles() : {}", Arrays.toString(activeProfiles));
    }

    @Override
    public void beforeGetDefaultProfiles(Environment environment) {
        log("beforeGetDefaultProfiles");
    }

    @Override
    public void afterGetDefaultProfiles(Environment environment, String[] defaultProfiles) {
        log("afterGetDefaultProfiles() : {}", Arrays.toString(defaultProfiles));
    }

    @Override
    public void beforeSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("beforeSetActiveProfiles() : {}", Arrays.toString(profiles));
    }

    @Override
    public void afterSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("afterSetActiveProfiles(profiles : {})", Arrays.toString(profiles));
    }

    @Override
    public void beforeAddActiveProfile(ConfigurableEnvironment environment, String profile) {
        log("beforeAddActiveProfile(profile : '{}')", profile);
    }

    @Override
    public void afterAddActiveProfile(ConfigurableEnvironment environment, String profile) {
        log("afterAddActiveProfile(profile : '{}')", profile);
    }

    @Override
    public void beforeSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("beforeSetDefaultProfiles(profiles : {})", Arrays.toString(profiles));
    }

    @Override
    public void afterSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("afterSetDefaultProfiles(profiles : {})", Arrays.toString(profiles));
    }

    @Override
    public void beforeGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object defaultValue) {
        log("beforeGetProperty(name : '{}', targetType : {} , defaultValue : {})", name, targetType, defaultValue);
    }

    @Override
    public void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object value, Object defaultValue) {
        log("afterGetProperty(name : '{}', targetType : {} , defaultValue : {}) : {}", name, targetType, defaultValue, value);
    }

    @Override
    public void beforeGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType) {
        log("beforeGetRequiredProperty(name : '{}', targetType : {})", name, targetType);
    }

    @Override
    public void afterGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object value) {
        log("afterGetRequiredProperty(name : '{}', targetType : {}) : {}", name, targetType, value);
    }

    @Override
    public void beforeResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {
        log("beforeResolvePlaceholders(text : '{}')", text);
    }

    @Override
    public void afterResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
        log("afterResolvePlaceholders(text : '{}') : '{}'", text, result);
    }

    @Override
    public void beforeResolveRequiredPlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {
        log("beforeResolveRequiredPlaceholders(text : '{}')", text);
    }

    @Override
    public void afterResolveRequiredPlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
        log("afterResolveRequiredPlaceholders(text : '{}') : '{}'", text, result);
    }

    @Override
    public void beforeSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {
        log("beforeSetRequiredProperties(properties : {})", Arrays.toString(properties));
    }

    @Override
    public void afterSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {
        log("afterSetRequiredProperties(properties : {})", Arrays.toString(properties));
    }

    @Override
    public void beforeValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {
        log("beforeValidateRequiredProperties()");
    }

    @Override
    public void afterValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {
        log("afterValidateRequiredProperties()");
    }

    @Override
    public void beforeGetConversionService(ConfigurablePropertyResolver propertyResolver) {
        log("beforeGetConversionService()");
    }

    @Override
    public void afterGetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
        log("afterGetConversionService() : {}", conversionService);
    }

    @Override
    public void beforeSetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
        log("beforeSetConversionService(conversionService : {})", conversionService);
    }

    @Override
    public void afterSetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
        log("afterSetConversionService(conversionService : {})", conversionService);
    }

    @Override
    public void beforeSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
        log("beforeSetPlaceholderPrefix(prefix : '{}')", prefix);
    }

    @Override
    public void afterSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
        log("afterSetPlaceholderPrefix(prefix : '{}')", prefix);
    }

    @Override
    public void beforeSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
        log("beforeSetPlaceholderSuffix(suffix : '{}')", suffix);
    }

    @Override
    public void afterSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
        log("afterSetPlaceholderSuffix(suffix : '{}')", suffix);
    }

    @Override
    public void beforeSetIgnoreUnresolvableNestedPlaceholders(ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {
        log("beforeSetIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders : {})", ignoreUnresolvableNestedPlaceholders);
    }

    @Override
    public void afterSetIgnoreUnresolvableNestedPlaceholders(ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {
        log("afterSetIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders : {})", ignoreUnresolvableNestedPlaceholders);
    }

    @Override
    public void beforeSetValueSeparator(ConfigurablePropertyResolver propertyResolver, String valueSeparator) {
        log("beforeSetValueSeparator(valueSeparator : '{}'}", valueSeparator);
    }

    @Override
    public void afterSetValueSeparator(ConfigurablePropertyResolver propertyResolver, String valueSeparator) {
        log("afterSetValueSeparator(valueSeparator : '{}'}", valueSeparator);
    }

    protected void log(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    protected void log(String messagePattern, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(messagePattern, args);
        }
    }
}
