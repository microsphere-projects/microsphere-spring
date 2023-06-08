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
package io.microsphere.spring.config.env.annotation;

import io.microsphere.spring.config.context.annotation.ResourcePropertySource;
import io.microsphere.spring.config.env.support.DefaultResourceComparator;
import io.microsphere.spring.config.env.support.JsonPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;

/**
 * The extension annotation of {ResourcePropertySource @ResourcePropertySource} providing a convenient and declarative
 * mechanism for adding a JSON {@link PropertySource} to Spring's Environment.
 * To be used in conjunction with {@link Configuration @Configuration} classes.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @see JsonPropertySourceFactory
 * @see MapPropertySource
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ResourcePropertySource(factory = JsonPropertySourceFactory.class)
public @interface JsonPropertySource {

    /**
     * Indicate the name of this property source.
     *
     * @see org.springframework.core.env.PropertySource#getName()
     * @see Resource#getDescription()
     */
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "name")
    String name() default "";

    /**
     * Indicates current {@link org.springframework.core.env.PropertySource} is first order or not If specified ,
     * {@link #before()} and {@link #after()} will be ignored, or last order.
     *
     * @return default value is <code>false</code>
     */
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "first")
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
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "before")
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
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "after")
    String after() default "";

    /**
     * Indicate the resource location(s) of the JSON file to be loaded.
     * <p>For example, {@code "classpath:/com/myco/app.json"}
     * <p>Resource location wildcards (e.g. *&#42;/*.json) also are permitted;
     * <p>${...} placeholders will be resolved against any/all property sources already
     * registered with the {@code Environment}.
     * <p>Each location will be added to the enclosing {@code Environment} as its own
     * property source, and in the order declared.
     */
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "value")
    String[] value() default {};

    /**
     * Indicate the resources to be sorted when {@link #value()} specifies the resource location wildcards
     * <p>For example, {@code "classpath:/com/myco/*.json"}, suppose there are two resources named
     * "a.json" and "b.json" where two instances of {@link Resource} will be resolved, they are
     * the sources of {@link org.springframework.core.env.PropertySource}, thus it has to sort
     * them to indicate the order of {@link org.springframework.core.env.PropertySource} that will be added to
     * the enclosing {@code Environment}.
     *
     * <p>Default is {@link DefaultResourceComparator}
     *
     * @see DefaultResourceComparator
     */
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "resourceComparator")
    Class<? extends Comparator<Resource>> resourceComparator() default DefaultResourceComparator.class;

    /**
     * Indicate if a failure to find a {@link #value property resource} should be
     * ignored.
     * <p>{@code true} is appropriate if the JSON file is completely optional.
     * <p>Default is {@code false}.
     */
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "ignoreResourceNotFound")
    boolean ignoreResourceNotFound() default false;

    /**
     * A specific character encoding for the given resources.
     * <p>Default is "UTF-8"
     */
    @AliasFor(annotation = ResourcePropertySource.class, attribute = "encoding")
    String encoding() default "UTF-8";
}
