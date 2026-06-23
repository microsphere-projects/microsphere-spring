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

package io.microsphere.spring.context;

import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.generateBeanName;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.constants.PropertyConstants.APPLICATION_CONTEXT_INITIALIZER_PROPERTY_NAME_PREFIX;

/**
 * Abstract class of {@link ApplicationContextInitializer}  for {@link ConfigurableApplicationContext}, which can be
 * enabled or disabled by configuration properties.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationContextInitializer
 * @see ConfigurableApplicationContext
 * @see ConfigurableEnvironment
 * @since 1.0.0
 */
public abstract class ConfigurableApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    protected final Logger logger = getLogger(getClass());

    private String beanName;

    @Override
    public final void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        if (!isEnabled(context, environment)) {
            logger.info("The {} was disabled, if it needs to be enabled, please set the property '{}' to 'true' .",
                    getClass(), getEnabledPropertyName());
            return;
        }
        if (isRegistered(context)) {
            logger.info("Current {} Bean[name : '{}'] is already registered to the Spring Context[id : '{}'] , so it will not be initialized.",
                    getClass(), getBeanName(), context.getId());
            return;
        }
        initialize(context, environment);
        registerSelf(context);
    }

    /**
     * Initialize the {@link ConfigurableApplicationContext}.
     *
     * @param context     the {@link ConfigurableApplicationContext}
     * @param environment the {@link ConfigurableEnvironment}
     */
    protected abstract void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment);

    /**
     * Is enabled or not
     *
     * @param context     the {@link ConfigurableApplicationContext}
     * @param environment the {@link ConfigurableEnvironment}
     * @return if enabled, return <code>true</code>, or <code>false</code>
     * @see #getEnabledPropertyName()
     * @see #getDefaultEnabled()
     */
    protected boolean isEnabled(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        String propertyName = getEnabledPropertyName();
        return environment.getProperty(propertyName, Boolean.class, getDefaultEnabled());
    }

    /**
     * Get the property name of the enabled
     *
     * @return the property name of the enabled
     * @see #getBeanName()
     */
    protected String getEnabledPropertyName() {
        return APPLICATION_CONTEXT_INITIALIZER_PROPERTY_NAME_PREFIX + getBeanName() + DOT + ENABLED_PROPERTY_NAME;
    }

    /**
     * Get the default value of enabled
     *
     * @return the default value of enabled
     * @see #getEnabledPropertyName()
     */
    protected boolean getDefaultEnabled() {
        return true;
    }

    /**
     * Get the bean name of this {@link ConfigurableApplicationContextInitializer}
     *
     * @return the bean name of this {@link ConfigurableApplicationContextInitializer}
     * @see BeanUtils#generateBeanName(Class)
     */
    protected String getBeanName() {
        String beanName = this.beanName;
        if (beanName == null) {
            beanName = generateBeanName(getClass());
            this.beanName = beanName;
        }
        return beanName;
    }

    protected boolean isRegistered(ConfigurableApplicationContext context) {
        return context.containsBean(getBeanName());
    }

    /**
     * Register self to the {@link ConfigurableApplicationContext}
     *
     * @param context the {@link ConfigurableApplicationContext}
     */
    protected void registerSelf(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
        registerBean(registry, getBeanName(), this);
    }
}
