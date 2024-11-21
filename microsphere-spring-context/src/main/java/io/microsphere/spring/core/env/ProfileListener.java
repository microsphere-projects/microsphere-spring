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

/**
 * The interface listens the manipulation of {@link ConfigurableEnvironment Environment's} profiles including:
 * <ul>
 *     <li>{@link ConfigurableEnvironment#setActiveProfiles(String...)}</li>
 *     <li>{@link ConfigurableEnvironment#addActiveProfile(String)}</li>
 *     <li>{@link ConfigurableEnvironment#setDefaultProfiles(String...)}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurableEnvironment
 * @since 1.0.0
 */
public interface ProfileListener {

    /**
     * Callback before {@link ConfigurableEnvironment#setActiveProfiles(String...) set active profiles}
     *
     * @param environment {@link ConfigurableEnvironment the underling Environment}
     * @param profiles    the profiles to be set
     */
    default void beforeSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
    }

    /**
     * Callback after {@link ConfigurableEnvironment#setActiveProfiles(String...) set active profiles}
     *
     * @param environment {@link ConfigurableEnvironment the underling Environment}
     * @param profiles    the profiles were set
     */
    default void afterSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
    }

    /**
     * Callback before {@link ConfigurableEnvironment#addActiveProfile(String) add an active profile}
     *
     * @param environment {@link ConfigurableEnvironment the underling Environment}
     * @param profile     the profile to be set
     */
    default void beforeAddActiveProfile(ConfigurableEnvironment environment, String profile) {
    }

    /**
     * Callback after {@link ConfigurableEnvironment#addActiveProfile(String) add an active profile}
     *
     * @param environment {@link ConfigurableEnvironment the underling Environment}
     * @param profile     the profile was set
     */
    default void afterAddActiveProfile(ConfigurableEnvironment environment, String profile) {
    }

    /**
     * Callback before {@link ConfigurableEnvironment#setDefaultProfiles(String...) set the default profiles}
     *
     * @param environment {@link ConfigurableEnvironment the underling Environment}
     * @param profiles    the profiles as the default to be set
     */
    default void beforeSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {
    }

    /**
     * Callback after {@link ConfigurableEnvironment#setDefaultProfiles(String...) set the default profiles}
     *
     * @param environment {@link ConfigurableEnvironment the underling Environment}
     * @param profiles    the profiles as the default were set
     */
    default void afterSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {
    }
}
