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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.constants.PropertyConstants;
import io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolver;
import io.microsphere.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static java.lang.Boolean.parseBoolean;

/**
 * An {@link ApplicationContextInitializer} implementation that initializes {@link ConfigurableEnvironment}
 * with {@link ListenableConfigurableEnvironment} to enable listening for environment changes.
 *
 * <h3>Configuration Properties</h3>
 * <ul>
 *     <li>{@code microsphere.spring.listenable-environment.enabled} -
 *         Whether to enable the {@link ListenableConfigurableEnvironment} (default: {@code false}).</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <p><strong>1. Configuration (application.properties):</strong></p>
 * <pre>
 * microsphere.spring.listenable-environment.enabled=true
 * </pre>
 *
 * <p><strong>2. Registration (Spring Boot 2.x - spring.factories):</strong></p>
 * <pre>{@code
 * org.springframework.context.ApplicationContextInitializer=\
 * io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer
 * }</pre>
 *
 * <p><strong>3. Programmatic Registration:</strong></p>
 * <pre>{@code
 * SpringApplication application = new SpringApplication(MyApplication.class);
 * application.addInitializers(new ListenableConfigurableEnvironmentInitializer());
 * application.run(args);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ListenableConfigurableEnvironment
 * @see EnvironmentListener
 * @see ProfileListener
 * @see PropertyResolverListener
 * @see ApplicationContextInitializer
 * @since 1.0.0
 */
public class ListenableConfigurableEnvironmentInitializer extends ConfigurableApplicationContextInitializer {

    /**
     * The prefix of the property name of {@link ListenableConfigurableEnvironment} : "microsphere.spring.listenable-environment."
     */
    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-environment.";

    private static final String DEFAULT_ENABLED = "false";

    /**
     * The property name of {@link ListenableConfigurableEnvironment} to be 'enabled' : "microsphere.spring.listenable-environment.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_ENABLED,
            description = "Whether to enable the ListenableConfigurableEnvironment"
    )
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The default property value of {@link ListenableConfigurableEnvironment} to be 'enabled'
     */
    public static final boolean DEFAULT_ENABLED_PROPERTY_VALUE = parseBoolean(DEFAULT_ENABLED);

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        context.setEnvironment(new ListenableConfigurableEnvironment(context));
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