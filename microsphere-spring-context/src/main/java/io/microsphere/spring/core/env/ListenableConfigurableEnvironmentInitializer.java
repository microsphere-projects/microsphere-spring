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

import io.microsphere.constants.PropertyConstants;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;

/**
 * The Initializer of {@link ListenableConfigurableEnvironment} based on {@link ApplicationContextInitializer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ApplicationContextInitializer
 * @since 1.0.0
 */
public class ListenableConfigurableEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "listenable-environment.";

    /**
     * The property name of {@link ListenableConfigurableEnvironment} to be 'enabled'
     */
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        if (!isEnabled(environment) && environment instanceof ListenableConfigurableEnvironment) {
            return;
        }
        applicationContext.setEnvironment(new ListenableConfigurableEnvironment(applicationContext));
    }

    public static boolean isEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty(ENABLED_PROPERTY_NAME, boolean.class, false);
    }
}
