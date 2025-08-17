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
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMapping.Builder;
import io.microsphere.spring.webflux.function.server.RequestPredicateVisitorAdapter;
import io.microsphere.spring.webflux.function.server.RouterFunctionVisitorAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.webflux;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        Collection<WebEndpointMapping> webEndpointMappings = new LinkedList<>();
        routerFunction.accept(new Adapter(webEndpointMappings::add));
        assertFalse(webEndpointMappings.isEmpty());
    }

    static class Adapter implements RequestPredicateVisitorAdapter, RouterFunctionVisitorAdapter {

        private final ThreadLocal<Entry<RequestPredicate, Builder<?>>> requestToBuilder = new ThreadLocal<>();

        private final Consumer<WebEndpointMapping> webEndpointMappingConsumer;

        Adapter(Consumer<WebEndpointMapping> webEndpointMappingConsumer) {
            this.webEndpointMappingConsumer = webEndpointMappingConsumer;
        }

        @Override
        public void method(Set<HttpMethod> methods) {
            doInBuilder(builder -> builder.methods(methods, HttpMethod::name));
        }

        @Override
        public void path(String pattern) {
            doInBuilder(builder -> builder.pattern(pattern));
        }

        @Override
        public void header(String name, String value) {
            doInBuilder(builder -> builder.header(name, value));
        }

        @Override
        public void queryParam(String name, String value) {
            doInBuilder(builder -> builder.param(name, value));
        }

        @Override
        public void startNested(RequestPredicate predicate) {
            getOrCreateEntry(predicate);
            predicate.accept(this);
        }

        @Override
        public void endNested(RequestPredicate predicate) {
            Entry<RequestPredicate, Builder<?>> entry = requestToBuilder.get();
            clearEntry(entry, predicate);
        }

        @Override
        public void route(RequestPredicate predicate, HandlerFunction<?> handlerFunction) {
            Entry<RequestPredicate, Builder<?>> entry = getOrCreateEntry(predicate);
            predicate.accept(this);
            clearEntry(entry, predicate);
        }

        private Entry<RequestPredicate, Builder<?>> getOrCreateEntry(RequestPredicate predicate) {
            Entry<RequestPredicate, Builder<?>> entry = requestToBuilder.get();
            if (entry == null) {
                Builder<?> builder = webflux(this);
                entry = ofEntry(predicate, builder);
                requestToBuilder.set(entry);
            }
            return entry;
        }

        private void clearEntry(Entry<RequestPredicate, Builder<?>> entry, RequestPredicate predicate) {
            if (entry != null) {
                if (entry.getKey() == predicate) {
                    requestToBuilder.remove();
                    Builder<?> builder = entry.getValue();
                    WebEndpointMapping webEndpointMapping = builder.build();
                    this.webEndpointMappingConsumer.accept(webEndpointMapping);
                }
            }
        }

        private void doInBuilder(Consumer<Builder<?>> builderConsumer) {
            Entry<RequestPredicate, Builder<?>> entry = requestToBuilder.get();
            if (entry != null) {
                Builder<?> builder = entry.getValue();
                builderConsumer.accept(builder);
            }
        }
    }
}

