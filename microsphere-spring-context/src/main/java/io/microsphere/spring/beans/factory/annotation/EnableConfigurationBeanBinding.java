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
package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.spring.context.config.ConfigurationBeanCustomizer;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.PropertySources;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enables Spring's annotation-driven configuration bean from {@link PropertySources properties}.
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Basic Configuration</h4>
 * <pre>{@code
 * @Configuration
 * @EnableConfigurationBeanBinding(prefix = "my.config", type = MyConfig.class)
 * public class MyConfig {}
 * }</pre>
 *
 * <h4>Multiple Bean Registration</h4>
 * <pre>{@code
 * @Configuration
 * @EnableConfigurationBeanBinding(prefix = "multi.config", type = MultiConfig.class, multiple = true)
 * public class MultiConfig {}
 * }</pre>
 *
 * <h4>Custom Ignore Behavior</h4>
 * <pre>{@code
 * @Configuration
 * @EnableConfigurationBeanBinding(
 *     prefix = "strict.config",
 *     type = StrictConfig.class,
 *     ignoreUnknownFields = false,
 *     ignoreInvalidFields = false
 * )
 * public class StrictConfig {}
 * }</pre>
 * Here, binding will fail if there are unknown or invalid fields in the configuration.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationBeanBindingRegistrar
 * @see ConfigurationBeanBindingPostProcessor
 * @see ConfigurationBeanCustomizer
 * @since 1.0.0
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(ConfigurationBeanBindingRegistrar.class)
@Repeatable(EnableConfigurationBeanBindings.class)
public @interface EnableConfigurationBeanBinding {

    /**
     * The default value for {@link #multiple()}
     *
     * @since 1.0.0
     */
    boolean DEFAULT_MULTIPLE = false;

    /**
     * The default value for {@link #ignoreUnknownFields()}
     *
     * @since 1.0.0
     */
    boolean DEFAULT_IGNORE_UNKNOWN_FIELDS = true;

    /**
     * The default value for {@link #ignoreInvalidFields()}
     *
     * @since 1.0.0
     */
    boolean DEFAULT_IGNORE_INVALID_FIELDS = true;

    /**
     * The name prefix of the properties that are valid to bind to the type of configuration.
     *
     * @return the name prefix of the properties to bind
     */
    String prefix();

    /**
     * @return The binding type of configuration.
     */
    Class<?> type();

    /**
     * It indicates whether {@link #prefix()} binding to multiple Spring Beans.
     *
     * @return the default value is <code>false</code>
     * @see #DEFAULT_MULTIPLE
     */
    boolean multiple() default DEFAULT_MULTIPLE;

    /**
     * Set whether to ignore unknown fields, that is, whether to ignore bind
     * parameters that do not have corresponding fields in the target object.
     * <p>Default is "true". Turn this off to enforce that all bind parameters
     * must have a matching field in the target object.
     *
     * @return the default value is <code>true</code>
     * @see #DEFAULT_IGNORE_UNKNOWN_FIELDS
     */
    boolean ignoreUnknownFields() default DEFAULT_IGNORE_UNKNOWN_FIELDS;

    /**
     * Set whether to ignore invalid fields, that is, whether to ignore bind
     * parameters that have corresponding fields in the target object which are
     * not accessible (for example because of null values in the nested path).
     * <p>Default is "true".
     *
     * @return the default value is <code>true</code>
     * @see #DEFAULT_IGNORE_INVALID_FIELDS
     */
    boolean ignoreInvalidFields() default DEFAULT_IGNORE_INVALID_FIELDS;
}
