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

import io.microsphere.annotation.Nonnull;
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
 * A strategy implementation of {@link OverrideAnnotationAttributesStrategy} that overrides annotation attributes
 * with values from Spring Environment configuration properties.
 * <p>
 * This strategy allows externalizing annotation attribute values into configuration properties (e.g., in {@code application.properties}
 * or {@code application.yml}). It maps annotation attributes to property keys using a configurable prefix.
 *
 * <h3>Property Mapping Logic</h3>
 * <ol>
 *     <li><strong>Prefix Determination:</strong> The property prefix is determined by:
 *         <ul>
 *             <li>Checking for a custom prefix defined by the property key:
 *                 {@code microsphere.spring.prefix.<AnnotationClassName>} (e.g., {@code microsphere.spring.prefix.org.springframework.context.annotation.PropertySource})
 *             </li>
 *             <li>Falling back to the default prefix:
 *                 {@code microsphere.spring.prefix.<AnnotationSimpleName>.} (e.g., {@code microsphere.spring.prefix.PropertySource.})
 *             </li>
 *         </ul>
 *     </li>
 *     <li><strong>Attribute Mapping:</strong> Each annotation attribute name is appended to the resolved prefix to form the full property key.
 *         For example, for an attribute {@code value}, the property key would be {@code <prefix>value}.
 *     </li>
 *     <li><strong>Type Conversion:</strong> If a configuration property exists for an attribute, it is converted to the attribute's type
 *         using the Spring {@link org.springframework.core.convert.ConversionService}. If conversion fails or the property is missing,
 *         the original annotation attribute value is retained.
 *     </li>
 * </ol>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>1. Default Prefix Behavior</h4>
 * Given an annotation {@code @PropertySource(name = "default", ignoreResourceNotFound = false)}:
 * <pre>{@code
 * // application.properties
 * microsphere.spring.prefix.PropertySource.name=my-custom-source
 * microsphere.spring.prefix.PropertySource.ignoreResourceNotFound=true
 *
 * // Resulting AnnotationAttributes:
 * // name = "my-custom-source"
 * // ignoreResourceNotFound = true
 * }</pre>
 *
 * <h4>2. Custom Prefix Behavior</h4>
 * You can define a custom prefix for a specific annotation type:
 * <pre>{@code
 * // application.properties
 * # Define a custom prefix for PropertySource annotation
 * microsphere.spring.prefix.org.springframework.context.annotation.PropertySource=app.prop.source.
 *
 * # Use the custom prefix to override attributes
 * app.prop.source.name=overridden-name
 * app.prop.source.ignoreResourceNotFound=true
 *
 * // Resulting AnnotationAttributes for @PropertySource:
 * // name = "overridden-name"
 * // ignoreResourceNotFound = true
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OverrideAnnotationAttributesStrategy
 * @see org.springframework.core.env.Environment
 * @since 1.0.0
 */
public class ConfigurationPropertyOverrideAnnotationAttributesStrategy implements OverrideAnnotationAttributesStrategy,
        EnvironmentAware {

    private static final Logger logger = getLogger(ConfigurationPropertyOverrideAnnotationAttributesStrategy.class);

    private ConfigurableEnvironment environment;

    /**
     * Overrides the original annotation attributes with values from configuration properties.
     * <p>
     * This method attempts to find configuration properties that match the annotation's attributes.
     * If a matching property is found and can be converted to the attribute's type, it replaces
     * the original value. Otherwise, the original value is retained.
     *
     * @param originalAttributes the original annotation attributes to be potentially overridden
     * @param annotationType     the type of the annotation being processed
     * @param annotationMetadata the metadata of the annotated element
     * @return a new {@link AnnotationAttributes} instance containing the overridden values,
     * or the original attributes if no overrides are applicabl
     */
    @Override
    public AnnotationAttributes override(AnnotationAttributes originalAttributes, Class<? extends Annotation> annotationType,
                                         AnnotationMetadata annotationMetadata) {
        Map<String, Object> configurationProperties = getConfigurationProperties(annotationType);
        if (configurationProperties.isEmpty()) {
            String propertyNamePrefix = getPropertyNamePrefix(annotationType);
            logger.warn("No configuration properties[prefix : '{}'] found for annotation : {}", propertyNamePrefix, annotationType);
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

    /**
     * Gets the configuration properties associated with the specified annotation type.
     * <p>
     * This method retrieves sub-properties from the environment using the property name prefix
     * derived from the given annotation type. These properties are intended to override
     * the default attributes of the annotation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Assume the environment contains the following properties:
     * // microsphere.spring.prefix.PropertySource.name = "myProperties"
     * // microsphere.spring.prefix.PropertySource.ignoreResourceNotFound = true
     *
     * Map<String, Object> properties = getConfigurationProperties(PropertySource.class);
     * // properties will contain:
     * // { "name": "myProperties", "ignoreResourceNotFound": true }
     * }
     * </pre>
     *
     * @param annotationType the type of the annotation for which to retrieve configuration properties
     * @return a map of configuration properties keyed by attribute name, or an empty map if none are found
     */
    @Nonnull
    protected Map<String, Object> getConfigurationProperties(Class<? extends Annotation> annotationType) {
        String propertyNamePrefix = getPropertyNamePrefix(annotationType);
        return getSubProperties(this.environment, propertyNamePrefix);
    }

    /**
     * Gets the full property name for the given annotation attribute.
     * <p>
     * The property name is constructed by concatenating the property name prefix
     * (obtained via {@link #getPropertyNamePrefix(Class)}) with the attribute name.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  String propertyName = getPropertyName(PropertySource.class, "factory");
     *  // propertyName == "microsphere.spring.prefix.PropertySource.factory"
     * }
     * </pre>
     *
     * @param annotationType the type of the annotation
     * @param attributeName  the name of the annotation attribute
     * @return the full property name corresponding to the annotation attribute
     */
    @Nonnull
    protected String getPropertyName(Class<? extends Annotation> annotationType, String attributeName) {
        return getPropertyNamePrefix(annotationType) + attributeName;
    }

    /**
     * Gets the property name prefix for the given annotation type.
     * <p>
     * This method determines the prefix by first checking if a specific prefix property is defined
     * in the environment using {@link #getPrefixPropertyName(Class)}. If not found, it falls back
     * to the default prefix generated by {@link #getDefaultPropertyNamePrefix(Class)}.
     * The resulting prefix is then normalized (ensuring it ends with a dot if not empty).
     *
     * <h3>Example Usage</h3>
     * <h4>Default</h4>
     * <pre>{@code
     *  String propertyNamePrefix = getPropertyNamePrefix(PropertySource.class);
     *  // propertyNamePrefix == "microsphere.spring.prefix.PropertySource."
     *  // the same as invocation on the method getDefaultPropertyNamePrefix(PropertySource.class)
     * }
     * </pre>
     * <h4>Customized Property Name Prefix</h4>
     * <pre>{@code
     *  // Assume the following environment property exists:
     *  // microsphere.spring.prefix.org.springframework.context.annotation.PropertySource = "microsphere.property-source."
     *  String propertyNamePrefix = getPropertyNamePrefix(PropertySource.class);
     *  // propertyNamePrefix == "microsphere.property-source."
     * }
     * </pre>
     *
     * @param annotationType the type of the annotation
     * @return the normalized property name prefix
     */
    @Nonnull
    protected String getPropertyNamePrefix(Class<? extends Annotation> annotationType) {
        String prefixPropertyName = getPrefixPropertyName(annotationType);
        String propertyNamePrefix = environment.getProperty(prefixPropertyName, String.class, getDefaultPropertyNamePrefix(annotationType));
        return normalizePrefix(propertyNamePrefix);
    }

    /**
     * Gets the property name used to look up the prefix for the given annotation type.
     * <p>
     * The property name is constructed by concatenating the {@link io.microsphere.spring.constants.PropertyConstants#PREFIX_PROPERTY_NAME_PREFIX}
     * with the fully qualified name of the annotation class.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  String prefixPropertyName = getPrefixPropertyName(PropertySource.class);
     *  // prefixPropertyName == "microsphere.spring.prefix.io.microsphere.spring.context.annotation.PropertySource"
     * }
     * </pre>
     *
     * @param annotationType the type of the annotation
     * @return the property name for the prefix
     */
    @Nonnull
    public static String getPrefixPropertyName(Class<? extends Annotation> annotationType) {
        return PREFIX_PROPERTY_NAME_PREFIX + annotationType.getName();
    }

    /**
     * Gets the default property name prefix for the given annotation type.
     * <p>
     * The default prefix is constructed by concatenating the {@link io.microsphere.spring.constants.PropertyConstants#PREFIX_PROPERTY_NAME_PREFIX}
     * with the simple name of the annotation class and a dot character.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     *  String defaultPropertyNamePrefix = getDefaultPropertyNamePrefix(PropertySource.class);
     *  // defaultPropertyNamePrefix == "microsphere.spring.prefix.PropertySource."
     * }
     * </pre>
     *
     * @param annotationType the type of the annotation
     * @return the default property name prefix
     */
    @Nonnull
    public static String getDefaultPropertyNamePrefix(Class<? extends Annotation> annotationType) {
        return PREFIX_PROPERTY_NAME_PREFIX + annotationType.getSimpleName() + DOT_CHAR;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = asConfigurableEnvironment(environment);
    }
}