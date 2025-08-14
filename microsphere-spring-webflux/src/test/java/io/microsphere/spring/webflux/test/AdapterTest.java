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

package io.microsphere.spring.webflux.test;

import io.microsphere.spring.test.web.controller.TestRestController;
import io.microsphere.spring.webflux.function.server.RequestPredicateVisitorAdapter;
import io.microsphere.spring.webflux.function.server.RouterFunctionVisitorAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * TODO
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TODO
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        AdapterTest.class,
        RouterFunctionTestConfig.class
})
@Import(TestRestController.class)
public class AdapterTest extends AbstractWebFluxTest {

    @Test
    void test() {
        RouterFunctionMapping routerFunctionMapping = context.getBean(RouterFunctionMapping.class);
        RouterFunction<?> routerFunction = routerFunctionMapping.getRouterFunction();
        routerFunction.accept(new Adapter());
    }

    static class Adapter implements RequestPredicateVisitorAdapter, RouterFunctionVisitorAdapter {

        @Override
        public void method(Set<HttpMethod> methods) {
            RequestPredicateVisitorAdapter.super.method(methods);
        }

        @Override
        public void path(String pattern) {
            RequestPredicateVisitorAdapter.super.path(pattern);
        }

        @Override
        public void pathExtension(String extension) {
            RequestPredicateVisitorAdapter.super.pathExtension(extension);
        }

        @Override
        public void header(String name, String value) {
            RequestPredicateVisitorAdapter.super.header(name, value);
        }

        @Override
        public void queryParam(String name, String value) {
            RequestPredicateVisitorAdapter.super.queryParam(name, value);
        }

        @Override
        public void startAnd() {
            RequestPredicateVisitorAdapter.super.startAnd();
        }

        @Override
        public void and() {
            RequestPredicateVisitorAdapter.super.and();
        }

        @Override
        public void endAnd() {
            RequestPredicateVisitorAdapter.super.endAnd();
        }

        @Override
        public void startOr() {
            RequestPredicateVisitorAdapter.super.startOr();
        }

        @Override
        public void or() {
            RequestPredicateVisitorAdapter.super.or();
        }

        @Override
        public void endOr() {
            RequestPredicateVisitorAdapter.super.endOr();
        }

        @Override
        public void startNegate() {
            RequestPredicateVisitorAdapter.super.startNegate();
        }

        @Override
        public void endNegate() {
            RequestPredicateVisitorAdapter.super.endNegate();
        }

        @Override
        public void startNested(RequestPredicate predicate) {
            predicate.accept(this);
            RouterFunctionVisitorAdapter.super.startNested(predicate);
        }

        @Override
        public void endNested(RequestPredicate predicate) {
            RouterFunctionVisitorAdapter.super.endNested(predicate);
        }

        @Override
        public void route(RequestPredicate predicate, HandlerFunction<?> handlerFunction) {
            predicate.accept(this);
            RouterFunctionVisitorAdapter.super.route(predicate, handlerFunction);
        }

        @Override
        public void resources(Function<ServerRequest, Mono<Resource>> lookupFunction) {
            RouterFunctionVisitorAdapter.super.resources(lookupFunction);
        }

        @Override
        public void unknown(RequestPredicate predicate) {
            RequestPredicateVisitorAdapter.super.unknown(predicate);
        }

        @Override
        public void attributes(Map<String, Object> attributes) {
            RouterFunctionVisitorAdapter.super.attributes(attributes);
        }

        @Override
        public void unknown(RouterFunction<?> routerFunction) {
            RouterFunctionVisitorAdapter.super.unknown(routerFunction);
        }
    }
}

