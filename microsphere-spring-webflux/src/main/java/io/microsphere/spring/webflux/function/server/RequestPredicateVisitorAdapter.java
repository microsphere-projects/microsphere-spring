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
import org.springframework.web.reactive.function.server.RequestPredicates.Visitor;

import java.util.Set;

/**
 * The adapter class for {@link Visitor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Visitor
 * @since 1.0.0
 */
public class RequestPredicateVisitorAdapter implements Visitor {

    @Override
    public void method(Set<HttpMethod> methods) {
    }

    @Override
    public void path(String pattern) {
    }

    @Override
    public void pathExtension(String extension) {
    }

    @Override
    public void header(String name, String value) {
    }

    @Override
    public void queryParam(String name, String value) {
    }

    @Override
    public void startAnd() {
    }

    @Override
    public void and() {
    }

    @Override
    public void endAnd() {
    }

    @Override
    public void startOr() {
    }

    @Override
    public void or() {
    }

    @Override
    public void endOr() {
    }

    @Override
    public void startNegate() {
    }

    @Override
    public void endNegate() {
    }

    @Override
    public void unknown(RequestPredicate predicate) {
    }
}
