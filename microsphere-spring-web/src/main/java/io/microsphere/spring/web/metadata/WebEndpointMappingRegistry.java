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
package io.microsphere.spring.web.metadata;

import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link WebEndpointMapping} Registry
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMapping
 * @see SimpleWebEndpointMappingRegistry
 * @since 1.0.0
 */
public interface WebEndpointMappingRegistry {

    /**
     * Registers an instance of {@link WebEndpointMapping}
     *
     * @param webEndpointMapping an instance of {@link WebEndpointMapping}
     * @return <code>true</code> if success, <code>false</code> otherwise
     */
    boolean register(WebEndpointMapping webEndpointMapping);

    /**
     * Registers the instances of {@link WebEndpointMapping}
     *
     * @param webEndpointMapping an instance of {@link WebEndpointMapping}
     * @param others             others of {@link WebEndpointMapping}
     * @return <code>true</code> if success, <code>false</code> otherwise
     */
    default int register(WebEndpointMapping webEndpointMapping, WebEndpointMapping... others) {
        int count = 0;
        if (register(webEndpointMapping)) {
            count++;
        }
        count += register(Arrays.asList(others));
        return count;
    }

    /**
     * Registers the instances of {@link WebEndpointMapping}
     *
     * @param webEndpointMappings the instances of {@link WebEndpointMapping}
     * @return the count of the registered instances of {@link WebEndpointMapping}
     */
    default int register(Iterable<WebEndpointMapping> webEndpointMappings) {
        int count = 0;
        Iterator<WebEndpointMapping> iterator = webEndpointMappings.iterator();
        while (iterator.hasNext()) {
            WebEndpointMapping webEndpointMapping = iterator.next();
            if (register(webEndpointMapping)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get all registered instances of {@link WebEndpointMapping}
     *
     * @return non-null
     */
    @NonNull
    Collection<WebEndpointMapping> getWebEndpointMappings();
}
