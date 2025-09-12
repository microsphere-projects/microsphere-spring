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
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RequestPredicates.Visitor;

import java.util.Set;

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

    @Override
    default void method(Set<HttpMethod> methods) {
    }

    @Override
    default void path(String pattern) {
    }

    @Override
    default void pathExtension(String extension) {
    }

    @Override
    default void header(String name, String value) {
    }

    @Override
    default void queryParam(String name, String value) {
    }

    @Override
    default void startAnd() {
    }

    @Override
    default void and() {
    }

    @Override
    default void endAnd() {
    }

    @Override
    default void startOr() {
    }

    @Override
    default void or() {
    }

    @Override
    default void endOr() {
    }

    @Override
    default void startNegate() {
    }

    @Override
    default void endNegate() {
    }

    @Override
    default void unknown(RequestPredicate predicate) {
    }

    default void visit(RequestPredicate predicate) {
        predicate.accept(this);
    }
}
