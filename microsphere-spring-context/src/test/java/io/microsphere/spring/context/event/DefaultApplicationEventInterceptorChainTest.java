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
package io.microsphere.spring.context.event;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link DefaultApplicationEventInterceptorChain} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultApplicationEventInterceptorChain
 * @see ApplicationEventInterceptorChain
 * @since 1.0.0
 */
class DefaultApplicationEventInterceptorChainTest {

    static class TestEvent extends ApplicationEvent {
        TestEvent(Object source) {
            super(source);
        }
    }

    @Test
    void testInterceptWithNoInterceptorsCallsConsumerDirectly() {
        AtomicReference<ApplicationEvent> capturedEvent = new AtomicReference<>();
        AtomicReference<ResolvableType> capturedType = new AtomicReference<>();

        DefaultApplicationEventInterceptorChain chain = new DefaultApplicationEventInterceptorChain(
                emptyList(),
                (event, type) -> {
                    capturedEvent.set(event);
                    capturedType.set(type);
                }
        );

        TestEvent event = new TestEvent(this);
        ResolvableType eventType = ResolvableType.forClass(TestEvent.class);

        chain.intercept(event, eventType);

        assertSame(event, capturedEvent.get());
        assertSame(eventType, capturedType.get());
    }

    @Test
    void testInterceptWithOneInterceptor() {
        AtomicInteger consumerCallCount = new AtomicInteger(0);
        AtomicInteger interceptorCallCount = new AtomicInteger(0);

        ApplicationEventInterceptor interceptor = (event, eventType, chain) -> {
            interceptorCallCount.incrementAndGet();
            chain.intercept(event, eventType);
        };

        DefaultApplicationEventInterceptorChain chain = new DefaultApplicationEventInterceptorChain(
                List.of(interceptor),
                (event, type) -> consumerCallCount.incrementAndGet()
        );

        chain.intercept(new TestEvent(this), ResolvableType.forClass(TestEvent.class));

        assertEquals(1, interceptorCallCount.get());
        assertEquals(1, consumerCallCount.get());
    }

    @Test
    void testInterceptorCanShortCircuitChain() {
        AtomicInteger consumerCallCount = new AtomicInteger(0);

        // This interceptor does NOT call chain.intercept(), so the consumer should never be invoked
        ApplicationEventInterceptor blockingInterceptor = (event, eventType, chain) -> {
            // intentionally not delegating to chain
        };

        DefaultApplicationEventInterceptorChain chain = new DefaultApplicationEventInterceptorChain(
                List.of(blockingInterceptor),
                (event, type) -> consumerCallCount.incrementAndGet()
        );

        chain.intercept(new TestEvent(this), ResolvableType.forClass(TestEvent.class));

        assertEquals(0, consumerCallCount.get());
    }

    @Test
    void testInterceptWithMultipleInterceptorsInOrder() {
        StringBuilder order = new StringBuilder();

        ApplicationEventInterceptor first = (event, eventType, chain) -> {
            order.append("1");
            chain.intercept(event, eventType);
        };
        ApplicationEventInterceptor second = (event, eventType, chain) -> {
            order.append("2");
            chain.intercept(event, eventType);
        };

        DefaultApplicationEventInterceptorChain chain = new DefaultApplicationEventInterceptorChain(
                List.of(first, second),
                (event, type) -> order.append("end")
        );

        chain.intercept(new TestEvent(this), ResolvableType.forClass(TestEvent.class));

        assertEquals("12end", order.toString());
    }
}