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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.DependencyDescriptor;

import static io.microsphere.reflect.FieldUtils.findField;

/**
 * {@link LoggingAutowireCandidateResolvingListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingAutowireCandidateResolvingListener
 * @since 1.0.0
 */
class LoggingAutowireCandidateResolvingListenerTest {

    private LoggingAutowireCandidateResolvingListener listener;

    private String testField;

    @BeforeEach
    void setUp() {
        this.listener = new LoggingAutowireCandidateResolvingListener();
    }

    @Test
    void testSuggestedValueResolvedWithNonNullValue() {
        DependencyDescriptor descriptor = new DependencyDescriptor(findField(getClass(), "testField"), false);
        // Should not throw even if trace logging is disabled
        listener.suggestedValueResolved(descriptor, "someExpression");
    }

    @Test
    void testSuggestedValueResolvedWithNullValue() {
        DependencyDescriptor descriptor = new DependencyDescriptor(findField(getClass(), "testField"), false);
        // Null suggestedValue should be silently ignored
        listener.suggestedValueResolved(descriptor, null);
    }

    @Test
    void testLazyProxyResolvedWithNonNullProxy() {
        DependencyDescriptor descriptor = new DependencyDescriptor(findField(getClass(), "testField"), false);
        // Should not throw
        listener.lazyProxyResolved(descriptor, "testBean", new Object());
    }

    @Test
    void testLazyProxyResolvedWithNullProxy() {
        DependencyDescriptor descriptor = new DependencyDescriptor(findField(getClass(), "testField"), false);
        // Null proxy should be silently ignored
        listener.lazyProxyResolved(descriptor, "testBean", null);
    }
}