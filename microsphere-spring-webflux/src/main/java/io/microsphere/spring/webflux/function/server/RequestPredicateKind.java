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

package io.microsphere.spring.webflux.function.server;

import io.microsphere.annotation.Nonnull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * The kind of {@link RequestPredicate} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestPredicates
 * @see RequestPredicates#path(String)
 * @see RequestPredicates#pathExtension(String)
 * @see RequestPredicates#method(HttpMethod)
 * @see RequestPredicates#queryParam(String, String)
 * @see RequestPredicates#accept(MediaType...)
 * @see RequestPredicates#contentType(MediaType...)
 * @see RequestPredicate#and(RequestPredicate)
 * @see RequestPredicate#or(RequestPredicate)
 * @see RequestPredicate#negate()
 * @see RequestPredicate#nest(ServerRequest)
 * @see RequestPredicate
 * @since 1.0.0
 */
public enum RequestPredicateKind {

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.HttpMethodPredicate
     */
    METHOD("org.springframework.web.reactive.function.server.RequestPredicates.HttpMethodPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.PathPatternPredicate
     */
    PATH("org.springframework.web.reactive.function.server.RequestPredicates.PathPatternPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.PathExtensionPredicate
     */
    PATH_EXTENSION("org.springframework.web.reactive.function.server.RequestPredicates.PathExtensionPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.HeadersPredicate
     */
    HEADERS("org.springframework.web.reactive.function.server.RequestPredicates.HeadersPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.QueryParamPredicate
     */
    QUERY_PARAM("org.springframework.web.reactive.function.server.RequestPredicates.QueryParamPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.AcceptPredicate
     */
    ACCEPT("org.springframework.web.reactive.function.server.RequestPredicates.AcceptPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.ContentTypePredicate
     */
    CONTENT_TYPE("org.springframework.web.reactive.function.server.RequestPredicates.ContentTypePredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.AndRequestPredicate
     */
    AND("org.springframework.web.reactive.function.server.RequestPredicates.AndRequestPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.OrRequestPredicate
     */
    OR("org.springframework.web.reactive.function.server.RequestPredicates.OrRequestPredicate"),

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.NegateRequestPredicate
     */
    NEGATE("org.springframework.web.reactive.function.server.RequestPredicates.NegateRequestPredicate"),

    /**
     * Unknown
     */
    UNKNOWN("");

    private final String implementationClassName;

    RequestPredicateKind(String implementationClassName) {
        this.implementationClassName = implementationClassName;
    }

    /**
     * Gets the implementation class name
     *
     * @return the implementation class name
     */
    @Nonnull
    public String getImplementationClassName() {
        return implementationClassName;
    }

    /**
     * Resolves the {@link RequestPredicateKind} from the given {@link RequestPredicate}
     *
     * @param predicate the instance of {@link RequestPredicate}
     * @return the {@link RequestPredicateKind} enum constant, if not found, return {@link #UNKNOWN}
     * @throws NullPointerException if the {@code predicate} is {@code null}
     * @see RequestPredicate
     */
    public static RequestPredicateKind valueOf(RequestPredicate predicate) {
        String className = predicate.getClass().getCanonicalName();
        for (RequestPredicateKind kind : values()) {
            if (kind.implementationClassName.equals(className)) {
                return kind;
            }
        }
        return UNKNOWN;
    }


}
