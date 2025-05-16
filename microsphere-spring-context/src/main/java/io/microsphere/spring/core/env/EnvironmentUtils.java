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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.core.convert.ConversionServiceUtils.getSharedInstance;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.util.Assert.isInstanceOf;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link Environment} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class EnvironmentUtils implements Utils {

    private static final Logger logger = getLogger(EnvironmentUtils.class);

    /**
     * Cast {@link Environment} to {@link ConfigurableEnvironment}
     *
     * @param environment {@link Environment}
     * @return {@link ConfigurableEnvironment}
     * @throws IllegalArgumentException If <code>environment</code> argument is not an instance of {@link ConfigurableEnvironment}
     */
    public static ConfigurableEnvironment asConfigurableEnvironment(Environment environment) throws IllegalArgumentException {
        isInstanceOf(ConfigurableEnvironment.class, environment,
                "The 'environment' argument is not a instance of ConfigurableEnvironment, is it running in Spring container?");
        return (ConfigurableEnvironment) environment;
    }

    /**
     * Get the properties from the specified {@link Environment} and property names
     *
     * @param environment   {@link Environment}
     * @param propertyNames the property names
     * @return non-null
     */
    @Nonnull
    public static Map<String, String> getProperties(Environment environment, String... propertyNames) {
        int length = length(propertyNames);
        if (length < 1) {
            return emptyMap();
        } else if (length == 1) {
            String propertyName = propertyNames[0];
            return hasText(propertyName) ? singletonMap(propertyName, environment.getProperty(propertyName)) : emptyMap();
        }
        return getProperties(environment, asList(propertyNames));
    }

    /**
     * Get the properties from the specified {@link Environment} and property names
     *
     * @param environment   {@link Environment}
     * @param propertyNames the property names
     * @return non-null
     */
    @Nonnull
    public static Map<String, String> getProperties(Environment environment, Iterable<String> propertyNames) {
        return getProperties(environment, ofSet(propertyNames));
    }

    /**
     * Get the properties from the specified {@link Environment} and property names
     *
     * @param environment   {@link Environment}
     * @param propertyNames the property names
     * @return non-null
     */
    @Nonnull
    public static Map<String, String> getProperties(Environment environment, Set<String> propertyNames) {
        Map<String, String> properties = new HashMap<>(propertyNames.size());
        propertyNames.stream().filter(StringUtils::hasText).forEach(name -> {
            properties.put(name, environment.getProperty(name));
        });
        return unmodifiableMap(properties);
    }

    /**
     * Get the {@link ConversionService} from the specified {@link Environment}
     *
     * @param environment {@link Environment}
     * @return {@link ConversionService} if found, or <code>null</code>
     */
    @Nullable
    public static ConversionService getConversionService(Environment environment) {
        ConversionService conversionService = null;
        if (environment instanceof ConfigurablePropertyResolver) {
            conversionService = ((ConfigurablePropertyResolver) environment).getConversionService();
            if (conversionService == null) {
                conversionService = getSharedInstance();
                logger.warn("ConversionService can't be resolved from Environment[class: {}], the shared ConversionService will be used!",
                        environment.getClass().getName());
            }
        }
        return conversionService;
    }

    /**
     * Resolve the comma delimited value to {@link List}
     *
     * @param environment         {@link Environment}
     * @param commaDelimitedValue the comma delimited value
     * @return non-null
     */
    @Nonnull
    public static List<String> resolveCommaDelimitedValueToList(Environment environment, String commaDelimitedValue) {
        List<String> values = resolvePlaceholders(environment, commaDelimitedValue, List.class);
        return values == null ? emptyList() : unmodifiableList(values);
    }

    /**
     * Resolve the placeholders
     *
     * @param environment   {@link Environment}
     * @param propertyValue the property value
     * @param targetType    the target type
     * @param <T>           the target type
     * @return the resolved value
     */
    @Nullable
    public static <T> T resolvePlaceholders(Environment environment, String propertyValue, Class<T> targetType) {
        return resolvePlaceholders(environment, propertyValue, targetType, null);
    }

    /**
     * Resolve the placeholders
     *
     * @param environment   {@link Environment}
     * @param propertyValue the property value
     * @param targetType    the target type
     * @param defaultValue  the default value
     * @param <T>           the target type
     * @return the resolved value
     */
    @Nullable
    public static <T> T resolvePlaceholders(Environment environment, String propertyValue, Class<T> targetType, T defaultValue) {
        if (propertyValue == null) {
            return defaultValue;
        }
        ConversionService conversionService = getConversionService(environment);
        if (conversionService == null) {
            return defaultValue;
        }
        final T targetValue;
        String resolvedPropertyValue = environment.resolvePlaceholders(propertyValue);
        if (conversionService.canConvert(String.class, targetType)) {
            targetValue = conversionService.convert(resolvedPropertyValue, targetType);
            logger.trace("The property value[origin : {} , resolved : {}] was converted to be {}(type :{})!", propertyValue, resolvedPropertyValue,
                    targetValue, targetType);

        } else {
            targetValue = defaultValue;
            logger.trace("The property value[origin : {} , resolved : {}] can't be converted to be the target type[{}], take the default value({}) as result!",
                    propertyValue, resolvedPropertyValue, targetValue, targetType);
        }
        return targetValue;
    }

    private EnvironmentUtils() {
    }
}
