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
package io.microsphere.spring.beans.factory;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterators.spliteratorUnknownSize;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.INSTANCE;

/**
 * The utilities class for {@link ObjectProvider}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class ObjectProviderUtils implements Utils {

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @param objectProvider the {@link ObjectProvider}
     * @param <T>            the type of instance
     * @return an instance of the bean, or {@code null} if not available
     * @throws BeansException in case of creation errors
     * @see ObjectProvider#getObject()
     * @see ObjectProvider#getIfAvailable()
     * @since Spring Framework 4.3
     */
    @Nullable
    public static <T> T getIfAvailable(ObjectProvider<T> objectProvider) {
        return objectProvider == null ? null : objectProvider.getIfAvailable();
    }

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @param objectProvider  the {@link ObjectProvider}
     * @param defaultSupplier a callback for supplying a default object
     *                        if none is present in the factory
     * @return an instance of the bean, or the supplied default object
     * if no such bean is available
     * @throws BeansException in case of creation errors
     * @see ObjectProvider#getIfAvailable(Supplier)
     * @since Spring Framework 5.0
     */
    @Nullable
    public static <T> T getIfAvailable(@Nullable ObjectProvider<T> objectProvider, @Nullable Supplier<T> defaultSupplier) throws BeansException {
        T dependency = getIfAvailable(objectProvider);
        return dependency != null ? dependency : defaultSupplier == null ? null : defaultSupplier.get();
    }

    /**
     * Consume an instance (possibly shared or independent) of the object
     * managed by this factory, if available.
     *
     * @param objectProvider     the {@link ObjectProvider}
     * @param dependencyConsumer a callback for processing the target object
     *                           if available (not called otherwise)
     * @throws BeansException in case of creation errors
     * @see ObjectProvider#getIfAvailable()
     * @since Spring Framework 5.0
     */
    public static <T> void ifAvailable(@Nullable ObjectProvider<T> objectProvider, @Nullable Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfAvailable(objectProvider);
        if (dependency != null && dependencyConsumer != null) {
            dependencyConsumer.accept(dependency);
        }
    }

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @return an instance of the bean, or {@code null} if not available or
     * not unique (i.e. multiple candidates found with none marked as primary)
     * @throws BeansException in case of creation errors
     * @see ObjectProvider#getObject()
     * @see ObjectProvider#getIfAvailable()
     * @since Spring Framework 4.3
     */
    @Nullable
    public static <T> T getIfUnique(@Nullable ObjectProvider<T> objectProvider) throws BeansException {
        return objectProvider == null ? null : objectProvider.getIfUnique();
    }

    /**
     * Return an instance (possibly shared or independent) of the object
     * managed by this factory.
     *
     * @param objectProvider  the {@link ObjectProvider}
     * @param defaultSupplier a callback for supplying a public static object
     *                        if no unique candidate is present in the factory
     * @return an instance of the bean, or the supplied public static object
     * if no such bean is available or if it is not unique in the factory
     * (i.e. multiple candidates found with none marked as primary)
     * @throws BeansException in case of creation errors
     * @see #getIfUnique(ObjectProvider)
     * @see ObjectProvider#getIfAvailable(Supplier)
     * @since Spring Framework 5.0
     */
    @Nullable
    public static <T> T getIfUnique(@Nullable ObjectProvider<T> objectProvider, @Nullable Supplier<T> defaultSupplier) throws BeansException {
        T dependency = getIfUnique(objectProvider);
        return dependency != null ? dependency : defaultSupplier == null ? null : defaultSupplier.get();
    }

    /**
     * Consume an instance (possibly shared or independent) of the object
     * managed by this factory, if unique.
     *
     * @param objectProvider     the {@link ObjectProvider}
     * @param dependencyConsumer a callback for processing the target object
     *                           if unique (not called otherwise)
     * @throws BeansException in case of creation errors
     * @see ObjectProvider#getIfAvailable()
     * @see ObjectProvider#ifUnique(Consumer)
     * @since Spring Framework 5.0
     */
    public static <T> void ifUnique(@Nullable ObjectProvider<T> objectProvider, @Nullable Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfUnique(objectProvider);
        if (dependency != null && dependencyConsumer != null) {
            dependencyConsumer.accept(dependency);
        }
    }

    /**
     * Return an {@link Iterator} over all matching object instances,
     * without specific ordering guarantees (but typically in registration order).
     *
     * @throws UnsupportedOperationException if the version of Spring Framework is less than 5.1
     * @see ObjectProvider#stream()
     * @since Spring Framework 5.1
     */
    @Nullable
    public static <T> Iterator<T> iterator(@Nullable ObjectProvider<T> objectProvider) {
        // As of 5.1, ObjectProvider extends Iterable and provides Stream support.
        if (objectProvider instanceof Iterable) {
            return ((Iterable<T>) objectProvider).iterator();
        }
        throw new UnsupportedOperationException("The method ObjectProvider#iterator() supported!");
    }

    /**
     * Return a sequential {@link Stream} over all matching object instances,
     * without specific ordering guarantees (but typically in registration order).
     *
     * @throws UnsupportedOperationException if the version of Spring Framework is less than 5.1
     * @see ObjectProvider#iterator()
     * @see ObjectProvider#orderedStream()
     * @since Spring Framework 5.1
     */
    @Nullable
    public static <T> Stream<T> stream(@Nullable ObjectProvider<T> objectProvider) {
        Iterator<T> iterator = iterator(objectProvider);
        Spliterator<T> spliterator = spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Return a sequential {@link Stream} over all matching object instances,
     * pre-ordered according to the factory's common order comparator.
     * <p>In a standard Spring application context, this will be ordered
     * according to {@link Ordered} conventions,
     * and in case of annotation-based configuration also considering the
     * {@link Order} annotation,
     * analogous to multi-element injection points of list/array type.
     *
     * @throws UnsupportedOperationException if the version of Spring Framework is less than 5.1
     * @see ObjectProvider#stream()
     * @see OrderComparator
     * @since Spring Framework 5.1
     */
    public static <T> Stream<T> orderedStream(@Nullable ObjectProvider<T> objectProvider) {
        return stream(objectProvider).sorted(INSTANCE);
    }

    private ObjectProviderUtils() {
    }
}
