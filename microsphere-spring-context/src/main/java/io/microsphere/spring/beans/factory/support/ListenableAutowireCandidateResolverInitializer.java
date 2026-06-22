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
package io.microsphere.spring.beans.factory.support;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.constants.PropertyConstants;
import io.microsphere.spring.context.ConfigurableApplicationContextInitializer;
import io.microsphere.spring.core.env.ListenableConfigurableEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolver.register;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static java.lang.Boolean.parseBoolean;

/**
 * An {@link ApplicationContextInitializer} implementation that registers a
 * {@link ListenableAutowireCandidateResolver} to provide extensible autowiring
 * capabilities within the Spring application context.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyConfig implements WebMvcConfigurer {
 *     // Configuration code...
 * }
 * }</pre>
 *
 * <p>This initializer should be registered with a {@link org.springframework.context.ApplicationContext}
 * to enable custom autowiring logic during the application context initialization phase.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListenableAutowireCandidateResolver
 * @see ApplicationContextInitializer
 * @since 1.0.0
 */
public class ListenableAutowireCandidateResolverInitializer extends ConfigurableApplicationContextInitializer {

    /**
     * The prefix of the property name of {@link ListenableConfigurableEnvironment} : "microsphere.spring.listenable-autowire-candidate-resolver."
     */
    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-autowire-candidate-resolver.";

    private static final String DEFAULT_ENABLED = "false";

    /**
     * The property name of {@link ListenableAutowireCandidateResolver} to be 'enabled' : "microsphere.spring.listenable-autowire-candidate-resolver.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_ENABLED,
            description = "Whether to enable the ListenableAutowireCandidateResolver"
    )
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The default property value of {@link ListenableAutowireCandidateResolver} to be 'enabled'
     */
    public static final boolean DEFAULT_ENABLED_PROPERTY_VALUE = parseBoolean(DEFAULT_ENABLED);

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        register(context);
    }

    @Override
    protected String getEnabledPropertyName() {
        return ENABLED_PROPERTY_NAME;
    }

    @Override
    protected boolean getDefaultEnabled() {
        return DEFAULT_ENABLED_PROPERTY_VALUE;
    }
}