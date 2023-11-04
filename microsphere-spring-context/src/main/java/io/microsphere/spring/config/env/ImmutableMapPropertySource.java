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
package io.microsphere.spring.config.env;

import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableMap;

/**
 * Immutable {@link MapPropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MapPropertySource
 * @since 1.0.0
 */
public class ImmutableMapPropertySource extends MapPropertySource {

    /**
     * Create a new immutable {@code MapPropertySource} with the given name and {@code Map}.
     *
     * @param name   the associated name
     * @param source the Map source (without {@code null} values in order to get
     *               consistent {@link #getProperty} and {@link #containsProperty} behavior)
     */
    public ImmutableMapPropertySource(String name, Map source) {
        super(name, immutableMap(source));
    }

    private static Map immutableMap(Map source) {
        Map result = null;
        synchronized (ImmutableMapPropertySource.class) {
            result = newMap(source);
        }
        return unmodifiableMap(result);
    }

    private static Map newMap(Map source) {
        if (source instanceof SortedMap) {
            return new TreeMap(source);
        } else if (source instanceof LinkedHashMap) {
            return new LinkedHashMap(source);
        } else if (source instanceof IdentityHashMap) {
            return new IdentityHashMap(source);
        } else {
            return new HashMap(source);
        }
    }
}
