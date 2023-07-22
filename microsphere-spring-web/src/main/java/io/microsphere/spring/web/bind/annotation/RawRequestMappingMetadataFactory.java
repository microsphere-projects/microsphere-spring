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
package io.microsphere.spring.web.bind.annotation;

import org.springframework.lang.NonNull;

/**
 * The factory interface for {@link RawRequestMappingMetadata}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface RawRequestMappingMetadataFactory<S> {

    /**
     * Create an instance of {@link RawRequestMappingMetadata}
     * from the specified source
     *
     * @param source the specified source, e.g
     *               {@link org.springframework.web.servlet.mvc.method.RequestMappingInfo} or
     *               {@link org.springframework.web.reactive.result.method.RequestMappingInfo}
     * @return non-null
     */
    @NonNull
    RawRequestMappingMetadata create(S source);
}
