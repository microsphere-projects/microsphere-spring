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

package io.microsphere.spring.webflux.annotation;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.constants.PropertyConstants;
import io.microsphere.spring.context.ConfigurableApplicationContextInitializer;
import io.microsphere.spring.core.env.ListenableConfigurableEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static java.lang.Boolean.parseBoolean;

/**
 * {@link ApplicationContextInitializer} class for {@link EnableWebFluxExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebFluxExtension
 * @see ApplicationContextInitializer
 * @since 1.0.0
 */
public class WebFluxExtensionInitializer extends ConfigurableApplicationContextInitializer {

    /**
     * The prefix of the property name of {@link ListenableConfigurableEnvironment} : "microsphere.spring.webflux."
     */
    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "webflux" + DOT;

    private static final String DEFAULT_ENABLED = "true";

    /**
     * Environment property that can be used to override when {@link EnableWebFluxExtension the extension features of
     * WebFlux is enabled}: "microsphere.spring.webflux.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_ENABLED,
            description = "Whether to enable the @EnableWebFluxExtension"
    )
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The default property value of {@link EnableWebFluxExtension @EnableWebFluxExtension} to be 'enabled'
     */
    public static final boolean DEFAULT_ENABLED_PROPERTY_VALUE = parseBoolean(DEFAULT_ENABLED);

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        registerBeanDefinition(context.getBeanFactory(), Config.class);
    }

    @Override
    public String getEnabledPropertyName() {
        return ENABLED_PROPERTY_NAME;
    }

    @Override
    public boolean getDefaultEnabled() {
        return DEFAULT_ENABLED_PROPERTY_VALUE;
    }

    @EnableWebFluxExtension
    static class Config {
    }
}
