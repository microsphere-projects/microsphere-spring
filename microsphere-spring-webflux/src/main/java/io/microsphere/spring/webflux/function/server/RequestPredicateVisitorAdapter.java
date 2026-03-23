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

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RequestPredicates.Visitor;

import java.util.Set;
import java.util.function.Predicate;

/**
 * The adapter interface for {@link Visitor RequestPredicates.Visitor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Visitor
 * @see RequestPredicate
 * @see RequestPredicates
 * @since 1.0.0
 */
public interface RequestPredicateVisitorAdapter extends Visitor {

    /**
     * Receive notification of an HTTP method predicate.
     *
     * @param methods the HTTP methods that make up the predicate
     * @see RequestPredicates#method(HttpMethod)
     * @see RequestPredicates.Visitor#method(Set)
     */
    @Override
    default void method(Set<HttpMethod> methods) {
    }

    /**
     * Receive notification of a path predicate.
     *
     * @param pattern the path pattern that makes up the predicate
     * @see RequestPredicates#path(String)
     * @see RequestPredicates.Visitor#path(String)
     */
    @Override
    default void path(String pattern) {
    }

    /**
     * Receive notification of a path extension predicate.
     *
     * @param extension the path extension that makes up the predicate
     * @see RequestPredicates#pathExtension(String)
     * @see RequestPredicates.Visitor#pathExtension(String)
     */
    @Override
    default void pathExtension(String extension) {
    }

    /**
     * Receive notification of an HTTP header predicate.
     *
     * @param name  the name of the HTTP header to check
     * @param value the desired value of the HTTP header
     * @see RequestPredicates#headers(Predicate)
     * @see RequestPredicates#contentType(MediaType...)
     * @see RequestPredicates#accept(MediaType...)
     * @see RequestPredicates.Visitor#header(String, String)
     */
    @Override
    default void header(String name, String value) {
    }

    /**
     * Receive notification of a query parameter predicate.
     *
     * @param name  the name of the query parameter
     * @param value the desired value of the parameter
     * @see RequestPredicates#queryParam(String, String)
     * @see RequestPredicates.Visitor#queryParam(String, String)
     */
    @Override
    default void queryParam(String name, String value) {
    }

    /**
     * {@inheritDoc}
     *
     * @since Spring Framework 7.0
     */
    default void version(String version) {
    }

    /**
     * Receive first notification of a logical AND predicate.
     * The first subsequent notification will contain the left-hand side of the AND-predicate;
     * followed by {@link #and()}, followed by the right-hand side, followed by {@link #endAnd()}.
     *
     * @see RequestPredicate#and(RequestPredicate)
     * @see RequestPredicates.Visitor#startAnd()
     */
    @Override
    default void startAnd() {
    }

    /**
     * Receive "middle" notification of a logical AND predicate.
     * The following notification contains the right-hand side, followed by {@link #endAnd()}.
     *
     * @see RequestPredicate#and(RequestPredicate)
     * @see RequestPredicates.Visitor#and()
     */
    @Override
    default void and() {
    }

    /**
     * Receive last notification of a logical AND predicate.
     *
     * @see RequestPredicate#and(RequestPredicate)
     * @see RequestPredicates.Visitor#endAnd()
     */
    @Override
    default void endAnd() {
    }

    /**
     * Receive first notification of a logical OR predicate.
     * The first subsequent notification will contain the left-hand side of the OR-predicate;
     * the second notification contains the right-hand side, followed by {@link #endOr()}.
     *
     * @see RequestPredicate#or(RequestPredicate)
     * @see RequestPredicates.Visitor#startOr()
     */
    @Override
    default void startOr() {
    }

    /**
     * Receive "middle" notification of a logical OR predicate.
     * The following notification contains the right-hand side, followed by {@link #endOr()}.
     *
     * @see RequestPredicate#or(RequestPredicate)
     * @see RequestPredicates.Visitor#or()
     */
    @Override
    default void or() {
    }

    /**
     * Receive last notification of a logical OR predicate.
     *
     * @see RequestPredicate#or(RequestPredicate)
     * @see RequestPredicates.Visitor#endOr()
     */
    @Override
    default void endOr() {
    }

    /**
     * Receive first notification of a negated predicate.
     * The first subsequent notification will contain the negated predicated, followed
     * by {@link #endNegate()}.
     *
     * @see RequestPredicate#negate()
     * @see RequestPredicates.Visitor#startNegate()
     */
    @Override
    default void startNegate() {
    }

    /**
     * Receive last notification of a negated predicate.
     *
     * @see RequestPredicate#negate()
     * @see RequestPredicates.Visitor#endNegate()
     */
    @Override
    default void endNegate() {
    }

    /**
     * Receive first notification of an unknown predicate.
     *
     * @see RequestPredicates.Visitor#unknown(RequestPredicate)
     */
    @Override
    default void unknown(RequestPredicate predicate) {
    }

    default void visit(RequestPredicate predicate) {
        predicate.accept(this);
    }
}
