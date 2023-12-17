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
package io.microsphere.spring.context.config;

import io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * The binder for the configuration bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface ConfigurationBeanBinder {

    String CONFIGURATION_PREFIX_ATTRIBUTE_NAME = "configurationPrefix";

    String USING_MULTIPLE_CONFIGURATION_ATTRIBUTE_NAME = "multipleConfiguration";

    String CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME = "configurationProperties";

    String IGNORE_UNKNOWN_FIELDS_ATTRIBUTE_NAME = "ignoreUnknownFields";

    String IGNORE_INVALID_FIELDS_ATTRIBUTE_NAME = "ignoreInvalidFields";

    String CONFIGURATION_BEAN_REFRESH_STRATEGY = "refreshStrategy";

    /**
     * Bind the properties in the {@link Environment} to Configuration bean under specified prefix.
     *
     * @param beanDefinition     bean definition
     * @param configurationBean       the bean of configuration
     */
    default void bind(BeanDefinition beanDefinition, Object configurationBean) {
        if (beanDefinition == null || configurationBean == null)
            return;

        Map<String, Object> configurationProperties = (Map<String, Object>) beanDefinition.getAttribute(CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME);

        bind(configurationProperties, beanDefinition, configurationBean);
    }

    /**
     * Bind the properties in the {@link Environment} to Configuration bean under specified prefix.
     *
     * @param configurationProperties the special configuration properties
     * @param beanDefinition     bean definition
     * @param configurationBean       the bean of configuration
     */
    default void bind(Map<String, Object> configurationProperties, BeanDefinition beanDefinition, Object configurationBean) {
        if (beanDefinition == null || configurationBean == null)
            return;

        boolean ignoreUnknownFields = (Boolean) beanDefinition.getAttribute(IGNORE_UNKNOWN_FIELDS_ATTRIBUTE_NAME);

        boolean ignoreInvalidFields = (Boolean) beanDefinition.getAttribute(IGNORE_INVALID_FIELDS_ATTRIBUTE_NAME);

        bind(configurationProperties, ignoreUnknownFields, ignoreInvalidFields, configurationBean);
    }

    /**
     * Bind the properties in the {@link Environment} to Configuration bean under specified prefix.
     *
     * @param configurationProperties The configuration properties
     * @param ignoreUnknownFields     whether to ignore unknown fields, the value is come
     *                                from the attribute of {@link EnableConfigurationBeanBinding#ignoreUnknownFields()}
     * @param ignoreInvalidFields     whether to ignore invalid fields, the value is come
     *                                from the attribute of {@link EnableConfigurationBeanBinding#ignoreInvalidFields()}
     * @param configurationBean       the bean of configuration
     */
    void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields, boolean ignoreInvalidFields, Object configurationBean);

    /**
     * Set the {@link ConversionService}
     *
     * @param conversionService {@link ConversionService}
     */
    default void setConversionService(@Nullable ConversionService conversionService) {
    }

}
