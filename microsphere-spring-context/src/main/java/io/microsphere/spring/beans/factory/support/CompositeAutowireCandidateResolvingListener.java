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

import org.springframework.beans.factory.config.DependencyDescriptor;

import java.util.LinkedList;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.ListUtils.forEach;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * The composite class for {@link AutowireCandidateResolvingListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AutowireCandidateResolvingListener
 * @since 1.0.0
 */
public class CompositeAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {

    private final List<AutowireCandidateResolvingListener> listeners = new LinkedList<>();

    public CompositeAutowireCandidateResolvingListener(List<AutowireCandidateResolvingListener> listeners) {
        isTrue(isNotEmpty(listeners), "The argument 'listeners' must not be empty!");
        this.addListeners(listeners);
    }

    public void addListeners(List<AutowireCandidateResolvingListener> listeners) {
        forEach(listeners, listener -> {
            notNull(listener, "The element 'listener' must not be null!");
            this.listeners.add(listener);
        });
        sort(this.listeners);
    }

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        forEach(listeners, listener -> listener.suggestedValueResolved(descriptor, suggestedValue));
    }

    @Override
    public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
        forEach(listeners, listener -> listener.lazyProxyResolved(descriptor, beanName, proxy));
    }

    @Override
    public void lazyProxyClassResolved(DependencyDescriptor descriptor, String beanName, Class<?> proxyClass) {
        forEach(listeners, listener -> listener.lazyProxyClassResolved(descriptor, beanName, proxyClass));
    }
}
