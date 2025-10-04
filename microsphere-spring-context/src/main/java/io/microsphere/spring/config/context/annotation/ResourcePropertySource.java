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
package io.microsphere.spring.config.context.annotation;

import io.microsphere.spring.config.env.support.DefaultResourceComparator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.PropertySourceFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A variant of the {@link PropertySource @PropertySource} annotation that has some limitations:
 * <ul>
 *     <li>The {@link PropertySource @PropertySource} annotation can't meta-annotate the another annotation</li>
 *     <li>The {@link PropertySource @PropertySource} annotation can't control the order of {@link org.springframework.core.env.PropertySource}</li>
 *     <li>The {@link PropertySource @PropertySource} annotation can't be {@link Inherited inherited}</li>
 *     <li>The {@link PropertySource#value PropertySource#value()} attribute does not support the {@link Resource resource} location wildcards</li>
 *     <li>The {@link PropertySource#encoding() PropertySource#encoding()} attribute does not specify the default encoding for the {@link Resource resource}</li>
 * </ul>
 * <p>
 * This annotation provides additional features not available with the standard {@link PropertySource}, such as:
 * <ul>
 *     <li>Support for meta-annotation usage</li>
 *     <li>Control over the order in which property sources are added to Spring's Environment</li>
 *     <li>Inheritance through the use of the {@link Inherited} annotation</li>
 *     <li>Support for resource location wildcards in the resource paths</li>
 *     <li>Ability to specify default encoding for property resources</li>
 * </ul>
 *
 * <h3>Examples Usage</h3>
 *
 * <h4>Basic Usage</h4>
 * To load a simple properties file from the classpath:
 * <pre>{@code
 * @ResourcePropertySource("classpath:/com/example/config/app.properties")
 * public class AppConfig {
 * }
 * }</pre>
 *
 * <h4>Specifying Order</h4>
 * To control the precedence of this property source, you can use the attributes like {@link #before()} or {@link #first()}:
 * <pre>{@code
 * // Make this property source first in the list
 * @ResourcePropertySource(value = "classpath:/com/example/config/app.properties", first = true)
 * public class AppConfig {
 * }
 * }</pre>
 *
 * <h4>Using Wildcards and Custom Comparator</h4>
 * When using wildcards to load multiple property files, you can also define the loading order via a custom comparator:
 * <pre>{@code
 * @ResourcePropertySource(
 *     value = "classpath:/com/example/config/*.properties",
 *     resourceComparator = MyCustomResourceComparator.class
 * )
 * public class AppConfig {
 * }
 * }</pre>
 *
 * <h4>Ignoring Missing Resources</h4>
 * If the property file is optional, you can choose to ignore missing files:
 * <pre>{@code
 * @ResourcePropertySource(
 *     value = "classpath:/com/example/config/app.properties",
 *     ignoreResourceNotFound = true
 * )
 * public class AppConfig {
 * }
 * }</pre>
 *
 * <h4>Encoding Support</h4>
 * You can also specify the encoding used when reading property files:
 * <pre>{@code
 * @ResourcePropertySource(
 *     value = "classpath:/com/example/config/app.properties",
 *     encoding = "ISO-8859-1"
 * )
 * public class AppConfig {
 * }
 * }</pre>
 *
 * <h4>Custom PropertySourceFactory</h4>
 * To customize how the property source is created, provide your own implementation of
 * {@link PropertySourceFactory}:
 * <pre>{@code
 * @ResourcePropertySource(
 *     value = "classpath:/com/example/config/app.properties",
 *     factory = MyCustomPropertySourceFactory.class
 * )
 * public class AppConfig {
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySource
 * @see org.springframework.core.env.PropertySource
 * @see Configuration
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@PropertySourceExtension
@Repeatable(ResourcePropertySources.class)
@Import(ResourcePropertySourceLoader.class)
public @interface ResourcePropertySource {

    /**
     * Indicate the name of this property source.
     *
     * @see org.springframework.core.env.PropertySource#getName()
     * @see Resource#getDescription()
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String name() default "";

    /**
     * It indicates the property source is auto-refreshed when the configuration is
     * changed.
     *
     * @return default value is <code>false</code>
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    boolean autoRefreshed() default false;

    /**
     * Indicates current {@link org.springframework.core.env.PropertySource} is first order or not If specified ,
     * {@link #before()} and {@link #after()} will be ignored, or last order.
     *
     * @return default value is <code>false</code>
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    boolean first() default false;

    /**
     * The relative order before specified {@link org.springframework.core.env.PropertySource}
     * <p>
     * If not specified , current {@link org.springframework.core.env.PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link org.springframework.core.env.PropertySource}, default value is the empty string
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String before() default "";

    /**
     * The relative order after specified {@link org.springframework.core.env.PropertySource}
     * <p>
     * If not specified , current {@link org.springframework.core.env.PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link org.springframework.core.env.PropertySource}, default value is the empty string
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String after() default "";

    /**
     * Indicate the resource location(s) of the property source file to be loaded.
     * <p>Both traditional and XML-based properties file formats are supported
     * &mdash; for example, {@code "classpath:/com/myco/app.properties"}
     * or {@code "file:/path/to/file.xml"}.
     * <p>Resource location wildcards (e.g. *&#42;/*.properties) also are permitted;
     * <p>${...} placeholders will be resolved against any/all property sources already
     * registered with the {@code Environment}.
     * <p>Each location will be added to the enclosing {@code Environment} as its own
     * property source, and in the order declared.
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String[] value() default {};

    /**
     * Indicate the resources to be sorted when {@link #value()} specifies the resource location wildcards
     * or the same resource names with the different absolute paths.
     * <p>For example, {@code "classpath:/com/myco/*.properties"}, suppose there are two resources named
     * "a.properties" and "b.properties" where two instances of {@link Resource} will be resolved, they are
     * the sources of {@link org.springframework.core.env.PropertySource}, thus it has to sort
     * them to indicate the order of {@link org.springframework.core.env.PropertySource} that will be added to
     * the enclosing {@code Environment}.
     *
     * <p>Default is {@link DefaultResourceComparator}
     *
     * @see DefaultResourceComparator
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;

    /**
     * Indicate if a failure to find a {@link #value property resource} should be
     * ignored.
     * <p>{@code true} is appropriate if the properties file is completely optional.
     * <p>Default is {@code false}.
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    boolean ignoreResourceNotFound() default false;

    /**
     * A specific character encoding for the given resources.
     * <p>Default is the property value of "file.encoding" if present, or "UTF-8"
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String encoding() default "${file.encoding:UTF-8}";

    /**
     * Specify a custom {@link PropertySourceFactory}, if any.
     * <p>By default, a default factory for standard resource files will be used.
     * <p>Default is {@link DefaultPropertySourceFactory}
     *
     * @see DefaultPropertySourceFactory
     * @see org.springframework.core.io.support.ResourcePropertySource
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    Class<? extends PropertySourceFactory> factory() default DefaultPropertySourceFactory.class;
}
