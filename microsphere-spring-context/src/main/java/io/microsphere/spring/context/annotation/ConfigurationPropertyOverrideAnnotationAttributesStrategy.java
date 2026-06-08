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

package io.microsphere.spring.context.annotation;

import io.microsphere.logging.Logger;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.constants.PropertyConstants.PREFIX_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.spring.core.env.EnvironmentUtils.getConversionService;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;
import static io.microsphere.spring.core.env.PropertySourcesUtils.normalizePrefix;

/**
 * The implementation of {@link OverrideAnnotationAttributesStrategy} based on the configuration properties.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OverrideAnnotationAttributesStrategy
 * @since 1.0.0
 */
public class ConfigurationPropertyOverrideAnnotationAttributesStrategy implements OverrideAnnotationAttributesStrategy,
        EnvironmentAware {

    private static final Logger logger = getLogger(ConfigurationPropertyOverrideAnnotationAttributesStrategy.class);

    private ConfigurableEnvironment environment;

    @Override
    public AnnotationAttributes override(Class<? extends Annotation> annotationType, AnnotationAttributes originalAttributes,
                                         AnnotationMetadata annotationMetadata) {
        Map<String, Object> configurationProperties = getConfigurationProperties(annotationType);
        if (configurationProperties.isEmpty()) {
            return originalAttributes;
        }
        ConversionService conversionService = getConversionService(this.environment);
        AnnotationAttributes newAttributes = new AnnotationAttributes(originalAttributes.size());
        for (Entry<String, Object> entry : originalAttributes.entrySet()) {
            String attributeName = entry.getKey();
            Object originalAttributeValue = entry.getValue();
            Object configurationPropertyValue = configurationProperties.get(attributeName);
            String propertyName = getPropertyName(annotationType, attributeName);
            if (configurationPropertyValue == null) {
                logger.trace("The configuration property[name : '{}'] is not found, use the original property value : {}",
                        propertyName, originalAttributeValue);
                newAttributes.put(attributeName, originalAttributeValue);
                continue;
            }
            Method attributeMethod = findMethod(annotationType, attributeName);
            Class<?> attributeType = attributeMethod.getReturnType();
            if (conversionService.canConvert(configurationPropertyValue.getClass(), attributeType)) {
                Object newPropertyValue = conversionService.convert(configurationPropertyValue, attributeType);
                newAttributes.put(attributeName, newPropertyValue);
                logger.info("The configuration property[name : '{}' , value : '{}'] is converted to the attribute[value : {} , type : '{}']",
                        propertyName, configurationPropertyValue, newPropertyValue, attributeType);
            } else {
                newAttributes.put(attributeName, originalAttributeValue);
                logger.warn("The configuration property[name : '{}' , value : '{}'] cannot be converted to the attribute type : {}",
                        propertyName, configurationPropertyValue, attributeType);
            }
        }
        return newAttributes;
    }

    Map<String, Object> getConfigurationProperties(Class<? extends Annotation> annotationType) {
        String propertyNamePrefix = getPropertyNamePrefix(annotationType);
        return getSubProperties(this.environment, propertyNamePrefix);
    }

    String getPropertyName(Class<? extends Annotation> annotationType, String attributeName) {
        return getPropertyNamePrefix(annotationType) + attributeName;
    }

    String getPropertyNamePrefix(Class<? extends Annotation> annotationType) {
        String prefixPropertyName = getPrefixPropertyName(annotationType);
        String propertyNamePrefix = environment.getProperty(prefixPropertyName, String.class, getDefaultPropertyNamePrefix(annotationType));
        return normalizePrefix(propertyNamePrefix);
    }

    static String getPrefixPropertyName(Class<? extends Annotation> annotationType) {
        return PREFIX_PROPERTY_NAME_PREFIX + annotationType.getName();
    }

    static String getDefaultPropertyNamePrefix(Class<? extends Annotation> annotationType) {
        return PREFIX_PROPERTY_NAME_PREFIX + annotationType.getSimpleName() + DOT_CHAR;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = asConfigurableEnvironment(environment);
    }
}