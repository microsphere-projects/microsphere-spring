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
package io.microsphere.spring.config.zookeeper.annotation;

import io.microsphere.spring.config.context.annotation.PropertySourceExtension;
import io.microsphere.spring.config.env.support.DefaultResourceComparator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.PropertySourceFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

/**
 * The annotation for Zookeeper {@link PropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@PropertySourceExtension
@Import(ZookeeperPropertySourceLoader.class)
public @interface ZookeeperPropertySource {

    /**
     * The name of Zookeeper {@link PropertySource}
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String name() default "";

    /**
     * It indicates the property source is auto-refreshed when the configuration is
     * changed.
     *
     * @return default value is <code>true</code>
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    boolean autoRefreshed() default true;

    /**
     * Indicates current {@link PropertySource} is first order or not If specified ,
     * {@link #before()} and {@link #after()} will be ignored, or last order.
     *
     * @return default value is <code>false</code>
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    boolean first() default false;

    /**
     * The relative order before specified {@link PropertySource}
     * <p>
     * If not specified , current {@link PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link PropertySource}, default value is the empty string
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String before() default "";

    /**
     * The relative order after specified {@link PropertySource}
     * <p>
     * If not specified , current {@link PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link PropertySource}, default value is the empty string
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String after() default "";

    /**
     * Indicate the resource paths(s) of the property source file to be loaded.
     * <p>For example, {@code "/com/myco/app.properties"}
     * or {@code "/path/to/file.xml"}.
     * <p>Resource paths wildcards (e.g. *&#42;/*.properties) also are permitted;
     * <p>${...} placeholders will be resolved against any/all property sources already
     * registered with the {@code Environment}.
     * <p>Each location will be added to the enclosing {@code Environment} as its own
     * property source, and in the order declared.
     */
    @AliasFor(annotation = PropertySourceExtension.class, attribute = "value")
    String[] value() default {};

    /**
     * Indicate the resource path(s) of the property source file to be loaded.
     * <p>For example, {@code "/com/myco/app.properties"}
     * or {@code "/path/to/file.xml"}.
     * <p>Resource paths wildcards (e.g. *&#42;/*.properties) also are permitted;
     * <p>${...} placeholders will be resolved against any/all property sources already
     * registered with the {@code Environment}.
     * <p>Each location will be added to the enclosing {@code Environment} as its own
     * property source, and in the order declared.
     */
    @AliasFor(annotation = PropertySourceExtension.class, attribute = "value")
    String[] path();

    /**
     * Indicate the resources to be sorted when {@link #value()} specifies the resource location wildcards
     * or the same resource names with the different absolute paths.
     * <p>For example, {@code "/com/myco/*.properties"}, suppose there are two resources named
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
     * Indicate if a failure to find a {@link #value() property resource} should be
     * ignored.
     * <p>{@code true} is appropriate if the properties file is completely optional.
     * <p>Default is {@code false}.
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    boolean ignoreResourceNotFound() default false;

    /**
     * A specific character encoding for the given resources.
     * <p>Default is "UTF-8"
     */
    @AliasFor(annotation = PropertySourceExtension.class)
    String encoding() default "UTF-8";

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

    /**
     * The string presenting connection to Zookeeper
     *
     * @return non-null
     */
    String connectString() default "127.0.0.1:2181";

}
