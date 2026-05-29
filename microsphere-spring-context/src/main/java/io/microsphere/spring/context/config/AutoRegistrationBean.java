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

package io.microsphere.spring.context.config;

import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.constants.PropertyConstants;
import io.microsphere.spring.context.annotation.EnableAutoRegistrationBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.spring.beans.BeanUtils.generateBeanName;
import static io.microsphere.spring.constants.PropertyConstants.AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;
import static io.microsphere.spring.constants.PropertyConstants.BEANS_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_VALUE;
import static io.microsphere.text.FormatUtils.format;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * The marker interface for auto-registration bean, the implemention class will be loaded by {@link SpringFactoriesLoader
 * Spring Factories SPI}, and then registered into Spring container as bean.
 * <p>
 * All instances of {@link AutoRegistrationBean} must be enabled by {@link EnableAutoRegistrationBean @EnableAutoRegistrationBean}
 * that annotates on any {@link Configuration @Configuration} class.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableAutoRegistrationBean
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
public interface AutoRegistrationBean extends Ordered {

    /**
     * The property name pattern of auto-registration bean : "microsphere.spring.beans.{bean-name}.auto-registered"
     */
    String AUTO_REGISTERED_BEAN_PROPERTY_NAME_PATTERN = BEANS_PROPERTY_NAME_PREFIX + "{}" + DOT +
            AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;

    /**
     * Determine whether the auto-registration bean is enabled.
     *
     * @param environment {@link ConfigurableEnvironment}
     * @return <code>true</code> as default
     * @see #AUTO_REGISTERED_BEAN_PROPERTY_NAME_PATTERN
     * @see PropertyConstants#DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE
     */
    default boolean isAutoRegistered(ConfigurableEnvironment environment) {
        String propertyName = getAutoRegisteredPropertyName(getBeanName());
        return environment.getProperty(propertyName, boolean.class, DEFAULT_AUTO_REGISTERED_VALUE);
    }

    /**
     * Get the name of Spring Bean.
     *
     * @return non-null, the default value is the uncapitalized simple name of the bean type.
     */
    @Nonnull
    default String getBeanName() {
        return generateBeanName(getBeanType());
    }

    /**
     * Get the type of Spring Bean.
     *
     * @return non-null, the default value is the class of implementation class.
     */
    @Nonnull
    default Class<? extends AutoRegistrationBean> getBeanType() {
        return getClass();
    }

    /**
     * Get the scope of Spring Bean.
     *
     * @return non-null, the default value is "singleton".
     * @see BeanDefinition#SCOPE_SINGLETON
     */
    @Nonnull
    default String getScope() {
        return SCOPE_SINGLETON;
    }

    /**
     * Customize the {@link BeanDefinition}
     *
     * @param beanDefinitionBuilder {@link BeanDefinitionBuilder}
     */
    default void customize(BeanDefinitionBuilder beanDefinitionBuilder) {
    }

    /**
     * Get the order value for bean registration.
     *
     * @return {@link Ordered#LOWEST_PRECEDENCE} as default
     */
    @Override
    default int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    /**
     * Get the description of auto-registration bean.
     *
     * @return non-null
     */
    @Nonnull
    default String getDescription() {
        return "AutoRegistrationBean {" +
                "name='" + getBeanName() + "'" +
                ", type=" + getBeanType().getName() +
                ", scope='" + getScope() + "'" +
                ", order=" + getOrder() +
                "}";
    }

    static String getAutoRegisteredPropertyName(String beanName) {
        return format(AUTO_REGISTERED_BEAN_PROPERTY_NAME_PATTERN, beanName);
    }
}
