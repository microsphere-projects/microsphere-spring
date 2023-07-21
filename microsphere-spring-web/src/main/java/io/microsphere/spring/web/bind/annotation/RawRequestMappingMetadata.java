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

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Raw meta-data class for the Spring's annotation {@link RequestMapping @RequestMapping}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMapping
 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfo
 * @see org.springframework.web.reactive.result.method.RequestMappingInfo
 * @since 1.0.0
 */
public class RawRequestMappingMetadata {

    private final String[] patterns;

    private final String[] methods;

    private final String[] params;

    private final String[] headers;

    private final String[] consumes;

    private final String[] produces;

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String... methods) {
        this(patterns, methods, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String... params) {
        this(patterns, methods, params, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String[] params,
                                     @Nullable String... headers) {
        this(patterns, methods, params, headers, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String[] params,
                                     @Nullable String[] headers,
                                     @Nullable String... consumes) {
        this(patterns, methods, params, headers, consumes, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String[] params,
                                     @Nullable String[] headers,
                                     @Nullable String[] consumes,
                                     @Nullable String... produces) {
        this.patterns = patterns;
        this.methods = methods;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }
}
