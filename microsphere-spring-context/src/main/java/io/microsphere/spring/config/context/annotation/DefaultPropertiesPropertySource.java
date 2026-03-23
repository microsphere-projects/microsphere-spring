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
import io.microsphere.spring.core.env.PropertySourcesUtils;
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
 * A variant of the {@link PropertySource @PropertySource} annotation for the {@link org.springframework.core.env.PropertySource PropertySource}
 * named {@link PropertySourcesUtils#DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME "defaultProperties"}.
 *
 * <p>
 * Annotation providing a convenient and declarative mechanism for adding a
 * {@link org.springframework.core.env.PropertySource PropertySource} to Spring's Environment.
 * To be used in conjunction with {@link Configuration @Configuration} classes.
 *
 * <h3>Example Usage</h3>
 * <pre>
 * {@code
 * @Configuration
 * @DefaultPropertiesPropertySource("classpath:com/myco/app.properties")
 * public class MyConfig {
 * }
 * }
 * </pre>
 *
 * <p><b>Multiple Locations Example:</b>
 * <pre>
 * {@code
 * @Configuration
 * @DefaultPropertiesPropertySource(value = {
 *     "classpath:com/myco/app1.properties",
 *     "file:/path/to/app2.properties"
 * })
 * public class MyConfig {
 * }
 * }
 * </pre>
 *
 * <p><b>Wildcard Usage Example:</b>
 * <pre>
 * {@code
 * @Configuration
 * @DefaultPropertiesPropertySource("classpath*:com/myco/*.properties")
 * public class MyConfig {
 * }
 * }
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySource
 * @see PropertySourcesUtils#DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME
 * @see ResourcePropertySource
 * @see org.springframework.core.env.PropertySource
 * @see Configuration
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@Repeatable(DefaultPropertiesPropertySources.class)
@Import(DefaultPropertiesPropertySourceLoader.class)
public @interface DefaultPropertiesPropertySource {

    /**
     * <em>Inlined properties</em> in the form of <em>key-value</em> pairs that
     * should be added to the Spring {@link org.springframework.core.env.PropertySource PropertySource}
     * named {@link PropertySourcesUtils#DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME "defaultProperties"}.
     * Multiple key-value pairs may be specified via a single <em>text block</em>.
     *
     * <h4>Supported Syntax</h4>
     * <p>The supported syntax for key-value pairs is the same as the
     * syntax defined for entries in a Java
     * {@linkplain java.util.Properties#load(java.io.Reader) properties file}:
     * <ul>
     * <li>{@code "key=value"}</li>
     * <li>{@code "key:value"}</li>
     * <li>{@code "key value"}</li>
     * </ul>
     * <p><strong>WARNING</strong>: although properties can be defined using any
     * of the above syntax variants and any number of spaces between the key and
     * the value, it is recommended that you use one syntax variant and consistent
     * spacing &mdash; for example, consider always using
     * {@code "key = value"} instead of {@code "key= value"}, {@code "key=value"},
     * etc. Similarly, if you define inlined properties using <em>text blocks</em>
     * you should consistently use text blocks for inlined properties.
     * The reason is that the exact strings you provide will be
     * used to determine the key for the context cache. Consequently, to benefit
     * from the context cache you must ensure that you define inlined properties
     * consistently.
     *
     * <h4>Examples</h4>
     * <pre class="code">
     * &#47;&#47; Using an array of strings
     * &#064;DefaultPropertiesPropertySource(properties = {
     *     "key1 = value1",
     *     "key2 = value2"
     * })
     * &#064;ContextConfiguration
     * class MyTests {
     *   // ...
     * }</pre>
     * <pre class="code">
     * &#47;&#47; Using a single text block
     * &#064;DefaultPropertiesPropertySource(properties = """
     *     key1 = value1
     *     key2 = value2
     *     """
     * )
     * &#064;ContextConfiguration
     * class MyTests {
     *   // ...
     * }</pre>
     * <h4>Precedence</h4>
     * <p>Properties declared via this attribute have higher precedence than
     * properties loaded from resource {@link #locations}.
     * <p>This attribute may be used in conjunction with {@link #value}
     * <em>or</em> {@link #locations}.
     *
     * @see #locations
     * @see org.springframework.core.env.PropertySource
     * @see PropertySourcesUtils#DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME
     */
    String[] properties() default {};

    /**
     * Alias for {@link #locations}.
     * <p>This attribute may <strong>not</strong> be used in conjunction with
     * {@link #locations}, but it may be used <em>instead</em> of {@link #locations}.
     *
     * @see #locations
     */
    @AliasFor("locations")
    String[] value() default {};

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
    @AliasFor("value")
    String[] locations() default {};

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
     * <p>Default is "UTF-8"
     */
    String encoding() default "UTF-8";

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