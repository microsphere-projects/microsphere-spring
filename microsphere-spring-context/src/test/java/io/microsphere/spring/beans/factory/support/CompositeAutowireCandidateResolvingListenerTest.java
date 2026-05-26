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
package io.microsphere.spring.beans.factory.support;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.DependencyDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.microsphere.reflect.FieldUtils.findField;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link CompositeAutowireCandidateResolvingListener} Test
 *
 * @see CompositeAutowireCandidateResolvingListener
 * @since 1.0.0
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class CompositeAutowireCandidateResolvingListenerTest {

    private String testField;

    @Test
    void testConstructorWithEmptyListThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompositeAutowireCandidateResolvingListener(emptyList()));
    }

    @Test
    void testConstructorWithNullElementThrowsException() {
        List<AutowireCandidateResolvingListener> listeners = new ArrayList<>();
        listeners.add(null);
        assertThrows(IllegalArgumentException.class,
                () -> new CompositeAutowireCandidateResolvingListener(listeners));
    }

    @Test
    void testSuggestedValueResolvedDelegatesToAllListeners() {
        AtomicInteger callCount = new AtomicInteger(0);
        AutowireCandidateResolvingListener l1 = new AutowireCandidateResolvingListener() {
            @Override
            public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
                callCount.incrementAndGet();
            }
        };
        AutowireCandidateResolvingListener l2 = new AutowireCandidateResolvingListener() {
            @Override
            public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
                callCount.incrementAndGet();
            }
        };

        CompositeAutowireCandidateResolvingListener composite =
                new CompositeAutowireCandidateResolvingListener(List.of(l1, l2));

        DependencyDescriptor descriptor = new DependencyDescriptor(findField(getClass(), "testField"), false);
        composite.suggestedValueResolved(descriptor, "someValue");

        assertEquals(2, callCount.get());
    }

    @Test
    void testLazyProxyResolvedDelegatesToAllListeners() {
        AtomicInteger callCount = new AtomicInteger(0);
        AutowireCandidateResolvingListener l1 = new AutowireCandidateResolvingListener() {
            @Override
            public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
                callCount.incrementAndGet();
            }
        };
        AutowireCandidateResolvingListener l2 = new AutowireCandidateResolvingListener() {
            @Override
            public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
                callCount.incrementAndGet();
            }
        };

        CompositeAutowireCandidateResolvingListener composite =
                new CompositeAutowireCandidateResolvingListener(List.of(l1, l2));

        DependencyDescriptor descriptor = new DependencyDescriptor(findField(getClass(), "testField"), false);
        composite.lazyProxyResolved(descriptor, "testBean", new Object());

        assertEquals(2, callCount.get());
    }

    @Test
    void testAddListeners() {
        AtomicInteger callCount = new AtomicInteger(0);
        AutowireCandidateResolvingListener l1 = new AutowireCandidateResolvingListener() {
            @Override
            public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
                callCount.incrementAndGet();
            }
        };
        AutowireCandidateResolvingListener l2 = new AutowireCandidateResolvingListener() {
            @Override
            public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
                callCount.incrementAndGet();
            }
        };

        CompositeAutowireCandidateResolvingListener composite =
                new CompositeAutowireCandidateResolvingListener(List.of(l1));

        // Add additional listener
        composite.addListeners(List.of(l2));

        DependencyDescriptor descriptor = new DependencyDescriptor(findField(getClass(), "testField"), false);
        composite.suggestedValueResolved(descriptor, "value");

        assertEquals(2, callCount.get());
    }
}
