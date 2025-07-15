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

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container annotation that holds multiple {@link DefaultPropertiesPropertySource @DefaultPropertiesPropertySource} annotations.
 * <p>
 * This annotation is used to declare multiple {@link DefaultPropertiesPropertySource} on a configuration class,
 * allowing the configuration of multiple property sources with default properties in a Spring application context.
 * </p>
 *
 * <h3>Examples Usage</h3>
 * <h4>Basic Usage</h4>
 * <pre>{@code
 * @DefaultPropertiesPropertySources(value = {
 *     @DefaultPropertiesPropertySource(name = "default1", properties = {"key1=value1", "key2=value2"}),
 *     @DefaultPropertiesPropertySource(name = "default2", properties = {"key3=value3", "key4=value4"})
 * })
 * public class MyConfig {
 * }
 * }</pre>
 *
 * <h4>Java 8+ @Repeatable Usage</h4>
 * <pre>{@code
 * @DefaultPropertiesPropertySource(name = "default1", properties = {"key1=value1", "key2=value2"})
 * @DefaultPropertiesPropertySource(name = "default2", properties = {"key3=value3", "key4=value4"})
 * public class MyConfig {
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultPropertiesPropertySource
 * @see DefaultPropertiesPropertySourcesLoader
 * @see org.springframework.core.env.PropertySource
 * @see Configuration
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(DefaultPropertiesPropertySourcesLoader.class)
public @interface DefaultPropertiesPropertySources {

    DefaultPropertiesPropertySource[] value();
}
