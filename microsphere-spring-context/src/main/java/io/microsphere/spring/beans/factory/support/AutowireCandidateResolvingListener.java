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

import io.microsphere.spring.util.SpringFactoriesLoaderUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;

import java.util.LinkedList;
import java.util.List;

import static io.microsphere.spring.util.BeanUtils.getSortedBeans;
import static io.microsphere.spring.util.SpringFactoriesLoaderUtils.loadFactories;
import static java.util.Collections.unmodifiableList;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * The Event Listener interface for the resolving processing including:
 * <ul>
 *     <li>{@link AutowireCandidateResolver#getSuggestedValue(DependencyDescriptor) resolving suggested value}</li>
 *     <li>{@link AutowireCandidateResolver#getLazyResolutionProxyIfNecessary(DependencyDescriptor, String) resolving lazy proxy}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListenableAutowireCandidateResolver
 * @see LoggingAutowireCandidateResolvingListener
 * @see AutowireCandidateResolver
 * @since 1.0.0
 */
public interface AutowireCandidateResolvingListener {

    /**
     * Loads {@link AutowireCandidateResolvingListener the listeners} from
     * {@link SpringFactoriesLoaderUtils#loadFactories(BeanFactory, Class) Spring SPI with extension} and
     * {@link ListableBeanFactory#getBeansOfType Beans}
     *
     * @param beanFactory {@link BeanFactory}
     * @return the {@link AnnotationAwareOrderComparator#sort(List) sorted} {@link List} of
     * {@link AutowireCandidateResolvingListener the listeners}
     * @see SpringFactoriesLoaderUtils#loadFactories(BeanFactory, Class)
     */
    static List<AutowireCandidateResolvingListener> loadListeners(@Nullable BeanFactory beanFactory) {
        List<AutowireCandidateResolvingListener> listeners = new LinkedList<>();
        // Add all Spring SPI with extension
        listeners.addAll(loadFactories(beanFactory, AutowireCandidateResolvingListener.class));
        // Add all Spring Beans if BeanFactory is available
        if (beanFactory instanceof ListableBeanFactory) {
            listeners.addAll(getSortedBeans((ListableBeanFactory) beanFactory, AutowireCandidateResolvingListener.class));
        }
        // Sort
        sort(listeners);
        return unmodifiableList(listeners);
    }

    /**
     * The event raised after {@link AutowireCandidateResolver#getSuggestedValue(DependencyDescriptor)} called
     *
     * @param descriptor     {@link DependencyDescriptor the descriptor for the target method parameter or field}
     * @param suggestedValue the value suggested (typically an expression String), or null if none found
     */
    default void suggestedValueResolved(DependencyDescriptor descriptor, @Nullable Object suggestedValue) {
    }

    /**
     * The event raised after {@link AutowireCandidateResolver#getLazyResolutionProxyIfNecessary(DependencyDescriptor, String)} called
     *
     * @param descriptor the descriptor for the target method parameter or field
     * @param beanName   the name of the bean that contains the injection point
     * @param proxy      the lazy resolution proxy for the actual dependency target,
     *                   or {@code null} if straight resolution is to be performed
     */
    default void lazyProxyResolved(DependencyDescriptor descriptor, @Nullable String beanName, @Nullable Object proxy) {
    }
}
