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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.spring.context.ConfigurableApplicationContextInitializer;
import io.microsphere.spring.context.config.AutoRegistrationBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.constants.PropertyConstants.AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;
import static io.microsphere.spring.constants.PropertyConstants.BEANS_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE;
import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_VALUE;

/**
 * {@link ApplicationContextInitializer} class for {@link AutoRegistrationBean}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableAutoRegistrationBean
 * @see AutoRegistrationBean
 * @since 1.0.0
 */
public class AutoRegistrationBeanInitializer extends ConfigurableApplicationContextInitializer {

    /**
     * Environment property that can be used to override when auto-registration of Spring Beans is enabled.
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    public static final String BEANS_AUTO_REGISTERED_PROEPRTY_NAME = BEANS_PROPERTY_NAME_PREFIX + AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        registerBeanDefinition(context.getBeanFactory(), Config.class);
    }

    @EnableAutoRegistrationBean
    static class Config {
    }

    @Override
    public String getEnabledPropertyName() {
        return BEANS_AUTO_REGISTERED_PROEPRTY_NAME;
    }

    @Override
    public boolean getDefaultEnabled() {
        return DEFAULT_AUTO_REGISTERED_VALUE;
    }
}