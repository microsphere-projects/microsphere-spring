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
package io.microsphere.spring.core.env;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

/**
 * The interface listens the manipulation of {@link ConfigurableEnvironment} including {@link ProfileListener}
 * and {@link PropertyResolverListener} :
 * <ul>
 *     <li>{@link ConfigurableEnvironment#getPropertySources()}</li>
 *     <li>{@link ConfigurableEnvironment#getSystemProperties()}</li>
 *     <li>{@link ConfigurableEnvironment#getSystemEnvironment()}</li>
 *     <li>{@link ConfigurableEnvironment#merge(ConfigurableEnvironment)}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ProfileListener
 * @see PropertyResolverListener
 * @see ConfigurableEnvironment
 * @since 1.0.0
 */
public interface EnvironmentListener extends ProfileListener, PropertyResolverListener {

    /**
     * Callback before {@link ConfigurableEnvironment#getPropertySources()}
     *
     * @param environment {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     */
    default void beforeGetPropertySources(ConfigurableEnvironment environment) {
    }

    /**
     * Callback after {@link ConfigurableEnvironment#getPropertySources()}
     *
     * @param environment     {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     * @param propertySources {@link MutablePropertySources} was returned by {@link ConfigurableEnvironment#getPropertySources()}
     */
    default void afterGetPropertySources(ConfigurableEnvironment environment, MutablePropertySources propertySources) {
    }

    /**
     * Callback before {@link ConfigurableEnvironment#getSystemProperties()}
     *
     * @param environment {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     */
    default void beforeGetSystemProperties(ConfigurableEnvironment environment) {
    }

    /**
     * Callback after {@link ConfigurableEnvironment#getSystemProperties()}
     *
     * @param environment      {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     * @param systemProperties the value of {@link System#getProperties()} was returned by {@link ConfigurableEnvironment#getSystemProperties()}
     */
    default void afterGetSystemProperties(ConfigurableEnvironment environment, Map<String, Object> systemProperties) {
    }

    /**
     * Callback before {@link ConfigurableEnvironment#getSystemEnvironment()}
     *
     * @param environment {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     */
    default void beforeGetSystemEnvironment(ConfigurableEnvironment environment) {
    }

    /**
     * Callback after {@link ConfigurableEnvironment#getSystemEnvironment()}
     *
     * @param environment                {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     * @param systemEnvironmentVariables the value of {@link System#getenv()} was returned by {@link ConfigurableEnvironment#getSystemEnvironment()}
     */
    default void afterGetSystemEnvironment(ConfigurableEnvironment environment, Map<String, Object> systemEnvironmentVariables) {
    }

    /**
     * Callback before {@link ConfigurableEnvironment#merge(ConfigurableEnvironment)}
     *
     * @param environment       {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     * @param parentEnvironment parent {@link ConfigurableEnvironment}
     */
    default void beforeMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
    }

    /**
     * Callback after {@link ConfigurableEnvironment#merge(ConfigurableEnvironment)}
     *
     * @param environment       {@link ConfigurableEnvironment the underlying ConfigurableEnvironment}
     * @param parentEnvironment parent {@link ConfigurableEnvironment}
     */
    default void afterMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
    }
}
