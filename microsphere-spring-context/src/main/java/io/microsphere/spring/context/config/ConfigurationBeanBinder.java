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

import io.microsphere.annotation.Nullable;
import io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * A binder interface for mapping configuration properties to a configuration bean.
 *
 * <p>It provides methods to bind properties from an environment into a target bean,
 * handling unknown and invalid fields according to the provided flags.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create or obtain an instance of ConfigurationBeanBinder
 * ConfigurationBeanBinder binder = new DefaultConfigurationBeanBinder();
 *
 * // Prepare the configuration properties map
 * Map<String, Object> properties = new HashMap<>();
 * properties.put("user.configKey", "configValue");
 *
 * // Target configuration bean
 * MyConfigurationBean configBean = new MyConfigurationBean();
 *
 * // Bind the properties into the bean
 * binder.bind(properties, true, false, configBean);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultConfigurationBeanBinder
 * @since 1.0.0
 */
public interface ConfigurationBeanBinder {

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
