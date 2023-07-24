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

import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static org.springframework.core.ResolvableType.forClass;

/**
 * The factory interface for {@link WebMappingDescriptor}
 *
 * @param <S> the source type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface WebMappingDescriptorFactory<S> {

    /**
     * Current factory supports the specified source or not
     *
     * @param source could be one of these :
     *               <ul>
     *               <li>{@link javax.servlet.ServletRegistration}</li>
     *               <li>{@link javax.servlet.FilterRegistration}</li>
     *               <li>{@link org.springframework.web.servlet.mvc.method.RequestMappingInfo}</li>
     *               <li>{@link org.springframework.web.reactive.result.method.RequestMappingInfo}</li>
     *               </ul>
     * @return <code>true</code> if supports, <code>false</code> otherwise
     */
    default boolean supports(S source) {
        return true;
    }

    /**
     * Create the instance of {@link WebMappingDescriptor}
     *
     * @param source could be one of these :
     *               <ul>
     *               <li>{@link javax.servlet.ServletRegistration}</li>
     *               <li>{@link javax.servlet.FilterRegistration}</li>
     *               <li>{@link org.springframework.web.servlet.mvc.method.RequestMappingInfo}</li>
     *               <li>{@link org.springframework.web.reactive.result.method.RequestMappingInfo}</li>
     *               </ul>
     * @return <code>null</code> if can't be created
     */
    @Nullable
    WebMappingDescriptor create(S source);


    /**
     * Get the type of source
     *
     * @return the type of source
     */
    @NonNull
    default Class<S> getSourceType() {
        return (Class<S>) forClass(getClass()).as(WebMappingDescriptorFactory.class).resolveGeneric(0);
    }
}
