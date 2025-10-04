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

import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMapping.Builder;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.UNKNOWN_SOURCE;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.webflux;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertTrue;
import static java.lang.ThreadLocal.withInitial;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * The adapter class of {@link RequestPredicateVisitorAdapter} and {@link RouterFunctionVisitorAdapter} to consume the
 * {@link WebEndpointMapping WebEndpointMappings} generated during the visiting process.
 *
 * <p>This class is designed to traverse Spring WebFlux router functions and their associated request predicates,
 * collecting metadata about web endpoints. It implements both visitor interfaces to capture information such as
 * HTTP methods, path patterns, headers, and query parameters.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a consumer to handle the generated WebEndpointMappings
 * Consumer<WebEndpointMapping> mappingConsumer = mapping -> {
 *     System.out.println("Endpoint: " + mapping.getEndpoint());
 *     System.out.println("Patterns: " + Arrays.toString(mapping.getPatterns()));
 *     System.out.println("Methods: " + Arrays.toString(mapping.getMethods()));
 * };
 *
 * // Create the adapter
 * ConsumingWebEndpointMappingAdapter adapter = new ConsumingWebEndpointMappingAdapter(mappingConsumer);
 *
 * // Visit a router function to collect endpoint mappings
 * RouterFunction<?> routerFunction = ...; // your router function
 * routerFunction.accept(adapter);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestPredicateVisitorAdapter
 * @see RouterFunctionVisitorAdapter
 * @see WebEndpointMapping
 * @since 1.0.0
 */
public class ConsumingWebEndpointMappingAdapter implements RequestPredicateVisitorAdapter, RouterFunctionVisitorAdapter {

    private final ThreadLocal<LinkedList<Entry<RequestPredicate, Builder<?>>>> nestedRequestPredicateToBuilderStack = withInitial(LinkedList::new);

    private final Consumer<WebEndpointMapping<?>> webEndpointMappingConsumer;

    private final Object source;

    public ConsumingWebEndpointMappingAdapter(Consumer<WebEndpointMapping<?>> webEndpointMappingConsumer) {
        this(webEndpointMappingConsumer, UNKNOWN_SOURCE);
    }

    public ConsumingWebEndpointMappingAdapter(Consumer<WebEndpointMapping<?>> webEndpointMappingConsumer, Object source) {
        this.webEndpointMappingConsumer = webEndpointMappingConsumer;
        this.source = source;
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
        visit(predicate);
    }

    @Override
    public void endNested(RequestPredicate predicate) {
        LinkedList<Entry<RequestPredicate, Builder<?>>> stack = popRequestPredicateToBuilder(predicate);
        if (isEmpty(stack)) {
            this.nestedRequestPredicateToBuilderStack.remove();
        }
    }

    @Override
    public void route(RequestPredicate predicate, HandlerFunction<?> handlerFunction) {
        Entry<RequestPredicate, Builder<?>> entry = pushRequestPredicateToBuilder(predicate);
        visit(predicate);
        popRequestPredicateToBuilder(predicate);
        buildAndConsumeWebEndpointMapping(entry, handlerFunction);
    }

    private Entry<RequestPredicate, Builder<?>> pushRequestPredicateToBuilder(RequestPredicate predicate) {
        LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getNestedRequestPredicateToBuilderStack();
        Entry<RequestPredicate, Builder<?>> entry = createRequestPredicateToBuilder(predicate);
        stack.push(entry);
        return entry;
    }

    private LinkedList<Entry<RequestPredicate, Builder<?>>> popRequestPredicateToBuilder(RequestPredicate predicateToTest) {
        LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getNestedRequestPredicateToBuilderStack();
        Entry<RequestPredicate, Builder<?>> entry = stack.pop();
        RequestPredicate predicate = entry.getKey();
        assertTrue(Objects.equals(predicateToTest, predicate), () -> format("The popped RequestPredicate[{}] does not match the actual one : {}", predicate, predicateToTest));
        return stack;
    }

    private void buildAndConsumeWebEndpointMapping(Entry<RequestPredicate, Builder<?>> entry, HandlerFunction<?> handlerFunction) {
        Builder builder = entry.getValue();
        builder.source(source)
                .endpoint(handlerFunction);
        WebEndpointMapping<?> webEndpointMapping = builder.build();
        this.webEndpointMappingConsumer.accept(webEndpointMapping);
    }

    private LinkedList<Entry<RequestPredicate, Builder<?>>> getNestedRequestPredicateToBuilderStack() {
        return this.nestedRequestPredicateToBuilderStack.get();
    }

    private Entry<RequestPredicate, Builder<?>> createRequestPredicateToBuilder(RequestPredicate predicate) {
        Builder<?> builder = webflux();
        return ofEntry(predicate, builder);
    }

    private void doInBuilderStack(BiConsumer<Optional<Builder<?>>, Builder<?>> prevAndCurrentBuilderConsumer) {
        LinkedList<Entry<RequestPredicate, Builder<?>>> stack = getNestedRequestPredicateToBuilderStack();
        int size = size(stack);

        Entry<RequestPredicate, Builder<?>> currentEntry = stack.getFirst();

        if (size == 1) {
            prevAndCurrentBuilderConsumer.accept(empty(), currentEntry.getValue());
            return;
        }

        Entry<RequestPredicate, Builder<?>> prevEntry = stack.get(1);
        prevAndCurrentBuilderConsumer.accept(of(prevEntry.getValue()), currentEntry.getValue());
    }
}
