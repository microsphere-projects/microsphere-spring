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
package io.microsphere.spring.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link Environment} 工具类
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class EnvironmentUtils {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentUtils.class);

    private EnvironmentUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    public static Map<String, String> getProperties(Environment environment, String... propertyNames) {
        int length = propertyNames == null ? 0 : propertyNames.length;
        if (length < 1) {
            return emptyMap();
        } else if (length == 1) {
            String propertyName = propertyNames[0];
            return hasText(propertyName) ? singletonMap(propertyName, environment.getProperty(propertyName)) : emptyMap();
        }
        return getProperties(environment, asList(propertyNames));
    }

    public static Map<String, String> getProperties(Environment environment, Iterable<String> propertyNames) {
        Set<String> propertyNamesSet = propertyNames instanceof Set ? (Set) propertyNames :
                propertyNames instanceof Collection ? new HashSet<>((Collection) propertyNames) :
                        stream(propertyNames.spliterator(), false).collect(toSet());
        return getProperties(environment, propertyNamesSet);
    }

    public static Map<String, String> getProperties(Environment environment, Set<String> propertyNames) {
        Map<String, String> properties = new HashMap<>(propertyNames.size());
        propertyNames.stream().filter(StringUtils::hasText).forEach(name -> {
            properties.put(name, environment.getProperty(name));
        });
        return unmodifiableMap(properties);
    }
}
