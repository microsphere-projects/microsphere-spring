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

package io.microsphere.spring.config.env;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import org.springframework.core.env.Environment;

import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ClassUtils.getTypeName;


/**
 * The template class for component that is enabled or disabled based on {@link Environment}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Environment
 * @since 1.0.0
 */
public interface EnvironmentEnabled {

    /**
     * Checks if this component is enabled based on the given {@link Environment}.
     *
     * <p>This method determines the enabled status by looking up the property defined by
     * {@link #getEnabledPropertyName()} in the provided environment. If the property is not found,
     * it falls back to the default value specified by {@link #getDefaultEnabled()}.
     *
     * <b>Example Usage</b>
     * <pre>{@code
     * // Assuming this interface is implemented by a class named MyService.
     * // The default property name would be "microsphere.spring.MyService.enabled".
     *
     * Environment environment = ...; // Obtain Environment
     * MyService myService = new MyService();
     *
     * // Check if enabled (defaults to true if property is absent)
     * boolean enabled = myService.isEnabled(environment);
     *
     * // To disable, set the property "microsphere.spring.MyService.enabled=false"
     * // in your environment configuration.
     * }</pre>
     *
     * @param environment the Spring {@link Environment} to check against, must not be {@code null}
     * @return {@code true} if the component is enabled, {@code false} otherwise
     * @see #getEnabledPropertyName()
     * @see #getDefaultEnabled()
     */
    default boolean isEnabled(@Nonnull Environment environment) {
        String enabledPropertyName = getEnabledPropertyName();
        boolean enabled = environment.getProperty(enabledPropertyName, boolean.class, getDefaultEnabled());
        Class<?> currentClass = getClass();
        Logger logger = getLogger(currentClass);
        if (enabled) {
            if (logger.isTraceEnabled()) {
                logger.trace("The {} is enabled, if it needs to be disabled[defalt : '{}'], please set the property '{}' to 'false' .",
                        currentClass, getDefaultEnabled(), getEnabledPropertyName());
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("The {} is disabled, if it needs to be enabled[defalt : '{}'], please set the property '{}' to 'true' .",
                        currentClass, getDefaultEnabled(), getEnabledPropertyName());
            }
        }
        return enabled;
    }

    /**
     * Gets the property name used to determine if this component is enabled.
     *
     * <p>The default implementation constructs the property name by combining
     * the module prefix, the implementing class name, and the enabled suffix.
     *
     * <b>Example Usage</b>
     * <pre>{@code
     * // Assuming the implementing class is named {@code DataProvider}
     * // The resolved property name will be: "microsphere.spring.DataProvider.enabled"
     *
     * DataProvider provider = ...;
     * String key = provider.getEnabledPropertyName();
     * boolean active = environment.getProperty(key, boolean.class);
     * }</pre>
     *
     * @return the property name key for checking the enabled status
     */
    default String getEnabledPropertyName() {
        String className = getTypeName(this);
        return MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + className + DOT + ENABLED_PROPERTY_NAME;
    }

    /**
     * Gets the default enabled status for this component.
     *
     * <p>This value is used as the fallback when the property defined by
     * {@link #getEnabledPropertyName()} is not present in the {@link Environment}.
     *
     * <b>Example Usage</b>
     * <pre>{@code
     * // Assuming the implementing class is named {@code MyService}
     * // And the property "microsphere.spring.MyService.enabled" is NOT set in the Environment.
     *
     * MyService service = ...;
     * boolean enabled = service.isEnabled(environment);
     * // enabled will be true (since getDefaultEnabled returns true).
     * }</pre>
     *
     * @return {@code true} if the component is enabled by default, {@code false} otherwise
     * @see #isEnabled(Environment)
     */
    default boolean getDefaultEnabled() {
        return true;
    }
}
