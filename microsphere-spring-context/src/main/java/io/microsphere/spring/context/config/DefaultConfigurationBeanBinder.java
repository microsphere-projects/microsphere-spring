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

import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.DataBinder;

import java.util.Map;

/**
 * The default {@link ConfigurationBeanBinder} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationBeanBinder
 * @since 1.0.0
 */
public class DefaultConfigurationBeanBinder implements ConfigurationBeanBinder {

    private ConversionService conversionService;

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields,
                     boolean ignoreInvalidFields, Object configurationBean) {
        DataBinder dataBinder = new DataBinder(configurationBean);
        // Set ignored*
        dataBinder.setIgnoreInvalidFields(ignoreUnknownFields);
        dataBinder.setIgnoreUnknownFields(ignoreInvalidFields);
        // Get properties under specified prefix from PropertySources
        // Convert Map to MutablePropertyValues
        MutablePropertyValues propertyValues = new MutablePropertyValues(configurationProperties);
        dataBinder.initDirectFieldAccess();
        dataBinder.setConversionService(conversionService);
        // Bind
        dataBinder.bind(propertyValues);
    }
}
