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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.ListUtils.newLinkedList;
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

        private final ThreadLocal<LinkedList<Entry<RequestPredicate, Builder<?>>>> nestedRequestPredicateToBuilderStack = new ThreadLocal<>();

        private final ThreadLocal<Entry<RequestPredicate, Builder<?>>> requestPredicateToBuilder = new ThreadLocal<>();

        private final Consumer<WebEndpointMapping> webEndpointMappingConsumer;

        Adapter(Consumer<WebEndpointMapping> webEndpointMappingConsumer) {
            this.webEndpointMappingConsumer = webEndpointMappingConsumer;
        }

        @Override
        public void method(Set<HttpMethod> methods) {
            doInBuilderStack((prev, current) -> {
                prev.ifPresent(current::nestMethods);
                current.methods(methods, HttpMethod::name);
            });
        }

        @Override
        public void path(String pattern) {
            doInBuilderStack((prev, current) -> {
                current.pattern(pattern);
                prev.ifPresent(current::nestPatterns);
            });
        }

        @Override
        public void header(String name, String value) {
            doInBuilderStack((prev, current) -> {
                prev.ifPresent(current::nestHeaders);
                current.header(name, value);
            });
        }

        @Override
        public void queryParam(String name, String value) {
            doInBuilderStack((prev, current) -> {
                prev.ifPresent(current::nestParams);
                current.param(name, value);
            });
        }

        @Override
        public void startNested(RequestPredicate predicate) {
            pushRequestPredicateToBuilder(predicate);
            predicate.accept(this);
        }

        @Override
        public void endNested(RequestPredicate predicate) {
            popRequestPredicateToBuilder(predicate);
            LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getNestedRequestPredicateToBuilderStack();
            if (isEmpty(stack)) {
                this.nestedRequestPredicateToBuilderStack.remove();
            }
        }

        @Override
        public void route(RequestPredicate predicate, HandlerFunction<?> handlerFunction) {
            Entry<RequestPredicate, Builder<?>> entry = pushRequestPredicateToBuilder(predicate);
            predicate.accept(this);
            popRequestPredicateToBuilder(predicate);
            buildAndConsumeWebEndpointMapping(entry, handlerFunction);
        }


        private Entry<RequestPredicate, Builder<?>> pushRequestPredicateToBuilder(RequestPredicate predicate) {
            LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getOrCreateNestedRequestPredicateToBuilderStack();
            Entry<RequestPredicate, Builder<?>> entry = createRequestPredicateToBuilder(predicate);
            stack.push(entry);
            return entry;
        }

        private void popRequestPredicateToBuilder(RequestPredicate predicate) {
            LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getNestedRequestPredicateToBuilderStack();
            if (stack != null) {
                Iterator<Entry<RequestPredicate, Builder<?>>> iterator = stack.iterator();
                while (iterator.hasNext()) {
                    Entry<RequestPredicate, Builder<?>> entry = iterator.next();
                    if (Objects.equals(predicate, entry.getKey())) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        private void buildAndConsumeWebEndpointMapping(Entry<RequestPredicate, Builder<?>> entry, HandlerFunction<?> handlerFunction) {
            Builder<?> builder = entry.getValue();
            builder.source(handlerFunction);
            WebEndpointMapping webEndpointMapping = builder.build();
            this.webEndpointMappingConsumer.accept(webEndpointMapping);
        }

        private LinkedList<Entry<RequestPredicate, Builder<?>>> getOrCreateNestedRequestPredicateToBuilderStack() {
            LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getNestedRequestPredicateToBuilderStack();
            if (stack == null) {
                stack = newLinkedList();
                this.nestedRequestPredicateToBuilderStack.set(stack);
            }
            return stack;
        }

        private LinkedList<Entry<RequestPredicate, Builder<?>>> getNestedRequestPredicateToBuilderStack() {
            return this.nestedRequestPredicateToBuilderStack.get();
        }

        private Entry<RequestPredicate, Builder<?>> createRequestPredicateToBuilder(RequestPredicate predicate) {
            Builder<?> builder = webflux(this);
            return ofEntry(predicate, builder);
        }

        private void doInBuilderStack(BiConsumer<Optional<Builder<?>>, Builder<?>> prevAndCurrentBuilderConsumer) {
            LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getNestedRequestPredicateToBuilderStack();
            int size = size(stack);
            if (size == 0) {
                return;
            }

            Entry<RequestPredicate, Builder<?>> currentEntry = stack.getFirst();

            if (size == 1) {
                prevAndCurrentBuilderConsumer.accept(Optional.empty(), currentEntry.getValue());
                return;
            }
            Entry<RequestPredicate, Builder<?>> prevEntry = stack.get(1);
            prevAndCurrentBuilderConsumer.accept(Optional.of(prevEntry.getValue()), currentEntry.getValue());
        }
    }
}

