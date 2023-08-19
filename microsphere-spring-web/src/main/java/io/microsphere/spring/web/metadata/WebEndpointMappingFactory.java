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

import java.util.Optional;

import static org.springframework.core.ResolvableType.forClass;

/**
 * The factory interface for {@link WebEndpointMapping}
 *
 * @param <E> the type of endpoint
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface WebEndpointMappingFactory<E> {

    /**
     * Current factory supports the specified endpoint or not
     *
     * @param endpoint could be one of these :
     *                 <ul>
     *                 <li>{@link javax.servlet.ServletRegistration}</li>
     *                 <li>{@link javax.servlet.FilterRegistration}</li>
     *                 <li>{@link org.springframework.web.servlet.mvc.method.RequestMappingInfo}</li>
     *                 <li>{@link org.springframework.web.reactive.result.method.RequestMappingInfo}</li>
     *                 </ul>
     * @return <code>true</code> if supports, <code>false</code> otherwise
     */
    default boolean supports(E endpoint) {
        return true;
    }

    /**
     * Create the instance of {@link WebEndpointMapping}
     *
     * @param endpoint could be one of these :
     *                 <ul>
     *                 <li>{@link javax.servlet.ServletRegistration}</li>
     *                 <li>{@link javax.servlet.FilterRegistration}</li>
     *                 <li>{@link org.springframework.web.servlet.mvc.method.RequestMappingInfo}</li>
     *                 <li>{@link org.springframework.web.reactive.result.method.RequestMappingInfo}</li>
     *                 </ul>
     * @return <code>WebEndpointMapping</code> if present
     */
    Optional<WebEndpointMapping<E>> create(E endpoint);

    /**
     * Get the type of source
     *
     * @return the type of source
     */
    @NonNull
    default Class<E> getSourceType() {
        return (Class<E>) forClass(getClass()).as(WebEndpointMappingFactory.class).resolveGeneric(0);
    }
}
