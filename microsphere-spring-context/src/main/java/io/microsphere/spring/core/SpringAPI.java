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
package io.microsphere.spring.core;

import org.springframework.lang.Nullable;

import static io.microsphere.spring.core.SpringVersion.SPRING_4_1;
import static io.microsphere.spring.core.SpringVersion.SPRING_5_3;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * The enumeration of Spring API
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Enum
 * @see SpringVersion
 * @since 1.0.0
 */
public enum SpringAPI {

    /**
     * {@link org.springframework.beans.factory.SmartInitializingSingleton} since Spring Framework 4.1
     */
    SMART_INITIALIZING_SINGLETON("org.springframework.beans.factory.SmartInitializingSingleton", SPRING_4_1),

    /**
     * {@link org.springframework.core.metrics.ApplicationStartup} since Spring Framework 5.3
     */
    APPLICATION_STARTUP("org.springframework.core.metrics.ApplicationStartup", SPRING_5_3),

    /**
     * The interface {@link org.springframework.core.metrics.ApplicationStartup} since Spring Framework 5.3
     */
    APPLICATION_STARTUP_AWARE("org.springframework.context.ApplicationStartupAware", SPRING_5_3);

    private final String className;

    private final SpringVersion since;

    private final Class<?> revolvedClass;

    SpringAPI(String className, SpringVersion since) {
        this.className = className;
        this.since = since;
        this.revolvedClass = resolveClass(className);
    }

    public String getClassName() {
        return className;
    }

    public SpringVersion getSince() {
        return since;
    }

    @Nullable
    public Class<?> getRevolvedClass() {
        return revolvedClass;
    }

    /**
     * Determine whether the API is present
     *
     * @return {@code true} if the API is present
     */
    public boolean isPresent() {
        return revolvedClass != null;
    }

}
