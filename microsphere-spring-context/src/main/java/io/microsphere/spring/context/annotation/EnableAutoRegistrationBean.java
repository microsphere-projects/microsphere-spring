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
import io.microsphere.spring.context.config.AutoRegistrationBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.spring.constants.PropertyConstants.AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;
import static io.microsphere.spring.constants.PropertyConstants.BEANS_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enable auto-registration Spring beans that implement the interface {@link AutoRegistrationBean} are loaded by
 * {@link SpringFactoriesLoader Spring Factories SPI}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AutoRegistrationBean
 * @see AutoRegistrationBeanRegistrar
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@Import(AutoRegistrationBeanRegistrar.class)
public @interface EnableAutoRegistrationBean {

    /**
     * Environment property that can be used to override when auto-registration of Spring Beans is enabled.
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    String BEANS_AUTO_REGISTERED_PROEPRTY_NAME = BEANS_PROPERTY_NAME_PREFIX + AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;
}