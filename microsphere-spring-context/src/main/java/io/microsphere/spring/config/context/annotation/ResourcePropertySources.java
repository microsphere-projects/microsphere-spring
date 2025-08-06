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

import io.microsphere.annotation.Nonnull;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A container annotation that holds multiple {@link ResourcePropertySource} annotations.
 *
 * <p>This annotation is used to apply multiple {@link ResourcePropertySource} configurations
 * to a single class. It supports the same attributes as the individual annotations and
 * serves as a way to group them logically.</p>
 *
 * <h3>Examples Usage</h3>
 * <h4>Basic Usage</h4>
 * <pre>{@code
 * @ResourcePropertySources(value = {
 *     @ResourcePropertySource(name = "default1", properties = {"key1=value1", "key2=value2"}),
 *     @ResourcePropertySource(name = "default2", properties = {"key3=value3", "key4=value4"})
 * })
 * public class MyConfig {
 * }
 * }</pre>
 *
 * <h4>Java 8+ {@link Repeatable @Repeatable} Usage</h4>
 * <pre>{@code
 * @ResourcePropertySource(name = "default1", properties = {"key1=value1", "key2=value2"})
 * @ResourcePropertySource(name = "default2", properties = {"key3=value3", "key4=value4"})
 * public class MyConfig {
 * }
 * }</pre>
 *
 * <p>This example demonstrates how to use the annotation to load properties from both
 * the classpath and an external file. The order of the annotations in the array defines
 * the precedence of the property sources, with the first having the lowest precedence
 * and the last having the highest.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Inherited
@Documented
@Import(ResourcePropertySourcesLoader.class)
public @interface ResourcePropertySources {

    /**
     * The several {@link ResourcePropertySource} annotations.
     */
    @Nonnull
    ResourcePropertySource[] value();
}
