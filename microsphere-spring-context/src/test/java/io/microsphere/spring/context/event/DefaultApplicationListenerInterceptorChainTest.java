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
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link DefaultApplicationListenerInterceptorChain} Test
 *
 * @see DefaultApplicationListenerInterceptorChain
 * @see ApplicationListenerInterceptorChain
 * @since 1.0.0
 */
class DefaultApplicationListenerInterceptorChainTest {

    static class TestEvent extends ApplicationEvent {
        TestEvent(Object source) {
            super(source);
        }
    }

    @Test
    void testInterceptWithNoInterceptorsCallsConsumerDirectly() {
        AtomicReference<ApplicationListener<?>> capturedListener = new AtomicReference<>();
        AtomicReference<ApplicationEvent> capturedEvent = new AtomicReference<>();

        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(
                emptyList(),
                (listener, event) -> {
                    capturedListener.set(listener);
                    capturedEvent.set(event);
                }
        );

        ApplicationListener<TestEvent> listener = event -> {};
        TestEvent event = new TestEvent(this);

        chain.intercept(listener, event);

        assertSame(listener, capturedListener.get());
        assertSame(event, capturedEvent.get());
    }

    @Test
    void testInterceptWithOneInterceptor() {
        AtomicInteger consumerCallCount = new AtomicInteger(0);
        AtomicInteger interceptorCallCount = new AtomicInteger(0);

        ApplicationListenerInterceptor interceptor = (listener, event, chain) -> {
            interceptorCallCount.incrementAndGet();
            chain.intercept(listener, event);
        };

        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(
                List.of(interceptor),
                (listener, event) -> consumerCallCount.incrementAndGet()
        );

        ApplicationListener<TestEvent> listener = event -> {};
        chain.intercept(listener, new TestEvent(this));

        assertEquals(1, interceptorCallCount.get());
        assertEquals(1, consumerCallCount.get());
    }

    @Test
    void testInterceptorCanShortCircuitChain() {
        AtomicInteger consumerCallCount = new AtomicInteger(0);

        // This interceptor does NOT call chain.intercept(), so the consumer should never be invoked
        ApplicationListenerInterceptor blockingInterceptor = (listener, event, chain) -> {
            // intentionally not delegating to chain
        };

        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(
                List.of(blockingInterceptor),
                (listener, event) -> consumerCallCount.incrementAndGet()
        );

        ApplicationListener<TestEvent> listener = event -> {};
        chain.intercept(listener, new TestEvent(this));

        assertEquals(0, consumerCallCount.get());
    }

    @Test
    void testInterceptWithMultipleInterceptorsInOrder() {
        StringBuilder order = new StringBuilder();

        ApplicationListenerInterceptor first = (listener, event, chain) -> {
            order.append("1");
            chain.intercept(listener, event);
        };
        ApplicationListenerInterceptor second = (listener, event, chain) -> {
            order.append("2");
            chain.intercept(listener, event);
        };

        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(
                List.of(first, second),
                (listener, event) -> order.append("end")
        );

        ApplicationListener<TestEvent> listener = event -> {};
        chain.intercept(listener, new TestEvent(this));

        assertEquals("12end", order.toString());
    }
}
