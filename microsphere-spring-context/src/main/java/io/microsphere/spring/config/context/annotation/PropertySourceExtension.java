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
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.PropertySourceFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Extension meta-annotation for Spring's {@link PropertySource @PropertySource} to overcome its limitations:
 * <ul>
 *     <li>The {@link PropertySource @PropertySource} annotation can't auto-refresh the {@link PropertySources property sources}</li>
 *     <li>The {@link PropertySource @PropertySource} annotation can't control the order of {@link org.springframework.core.env.PropertySource}</li>
 *     <li>The {@link PropertySource @PropertySource} annotation can't be {@link Inherited inherited}</li>
 *     <li>The {@link PropertySource#value PropertySource#value()} attribute does not support the {@link Resource resource} location wildcards</li>
 *     <li>The {@link PropertySource#encoding() PropertySource#encoding()} attribute does not specify the default encoding for the {@link Resource resource}</li>
 * </ul>
 *
 * <h3>Features:</h3>
 * <ul>
 *     <li>Supports auto-refreshing property sources when configurations change</li>
 *     <li>Allows specifying the order of property sources using:
 *         <ul>
 *             <li>{@link #first()} - Place this property source at the top</li>
 *             <li>{@link #before()} - Place before a specific property source</li>
 *             <li>{@link #after()} - Place after a specific property source</li>
 *         </ul>
 *     </li>
 *     <li>Supports inheritance via the {@link Inherited @Inherited} annotation</li>
 *     <li>Resource location wildcards are supported in the {@link #value()} attribute</li>
 *     <li>Provides control over resource loading behavior with:
 *         <ul>
 *             <li>{@link #ignoreResourceNotFound()}</li>
 *             <li>{@link #encoding()}</li>
 *             <li>{@link #resourceComparator()}</li>
 *         </ul>
 *     </li>
 *     <li>Customizable property source creation via the {@link #factory()} attribute</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <h4>Basic Usage</h4>
 * <pre>{@code
 * // Define a custom annotation using PropertySourceExtension
 * @Target(ElementType.TYPE)
 * @Retention(RetentionPolicy.RUNTIME)
 * @PropertySourceExtension(
 *     value = "classpath:/my-config/*.properties",
 *     first = true,
 *     autoRefreshed = true,
 *     encoding = "UTF-8",
 *     ignoreResourceNotFound = true,
 *     resourceComparator = MyCustomResourceComparator.class
 * )
 * public @interface MyCustomPropertySource {
 * }
 *
 * // Use the custom annotation on a configuration class
 * @MyCustomPropertySource
 * @Configuration
 * public class MyConfig {
 * }
 * }</pre>
 *
 * <h4>Advanced Usage</h4>
 * <pre>{@code
 * @Target(TYPE)
 * @Retention(RUNTIME)
 * @Inherited
 * @Documented
 * @PropertySourceExtension
 * @Repeatable(ResourcePropertySources.class)
 * @Import(ResourcePropertySourceLoader.class)
 * public @interface ResourcePropertySource {
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     String name() default "";
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     boolean autoRefreshed() default false;
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     boolean first() default false;
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     String before() default "";
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     String after() default "";
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     String[] value() default {};
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     boolean ignoreResourceNotFound() default false;
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     String encoding() default "${file.encoding:UTF-8}";
 *
 *     @AliasFor(annotation = PropertySourceExtension.class)
 *     Class<? extends PropertySourceFactory> factory() default DefaultPropertySourceFactory.class;
 * }
 * }</pre>
 *
 * <p>This annotation is designed to be used as a meta-annotation for creating custom annotations that extend the functionality
 * of Spring's built-in {@link PropertySource @PropertySource}. It provides more flexibility and control over how properties
 * are loaded into the Spring environment.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySource
 * @see ResourcePropertySource
 * @see org.springframework.core.env.PropertySource
 * @see PropertySourceExtensionLoader
 * @since 1.0.0
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
public @interface PropertySourceExtension {

    /**
     * Indicate the name of this property source.
     *
     * @see org.springframework.core.env.PropertySource#getName()
     * @see Resource#getDescription()
     */
    String name() default "";

    /**
     * It indicates the property source is auto-refreshed when the configuration is
     * changed.
     *
     * @return default value is <code>false</code>
     */
    boolean autoRefreshed() default false;

    /**
     * Indicates current {@link org.springframework.core.env.PropertySource} is first order or not If specified ,
     * {@link #before()} and {@link #after()} will be ignored, or last order.
     *
     * @return default value is <code>false</code>
     */
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
    String after() default "";

    /**
     * Indicate the resource(s) of the property source to be loaded.
     * <p>For example, {@code "classpath:/com/myco/app.properties"}
     * or {@code "file:/path/to/file.xml"}.
     * <p>Resource wildcards (e.g. *&#42;/*.properties) also are permitted;
     * <p>${...} placeholders will be resolved against any/all property sources already
     * registered with the {@code Environment}.
     * <p>Each value will be added to the enclosing {@code Environment} as its own
     * property source, and in the order declared.
     */
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
    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;

    /**
     * Indicate if a failure to find a {@link #value property resource} should be
     * ignored.
     * <p>{@code true} is appropriate if the properties file is completely optional.
     * <p>Default is {@code false}.
     */
    boolean ignoreResourceNotFound() default false;

    /**
     * A specific character encoding for the given resources.
     * <p>Default is the property value of "file.encoding" if present, or "UTF-8"
     */
    String encoding() default "${file.encoding:UTF-8}";

    /**
     * Specify a custom {@link PropertySourceFactory}, if any.
     * <p>By default, a default factory for standard resource files will be used.
     * <p>Default is {@link DefaultPropertySourceFactory}
     *
     * @see DefaultPropertySourceFactory
     * @see org.springframework.core.io.support.ResourcePropertySource
     */
    Class<? extends PropertySourceFactory> factory() default DefaultPropertySourceFactory.class;
}
