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

import io.microsphere.annotation.Nonnull;
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
     * Retrieve the object managed by the given {@link ObjectProvider} if available,
     * without throwing an exception in case of no such bean being present.
     *
     * <p>This method is useful when you want to safely obtain a bean instance
     * and handle the absence of it gracefully.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * MyService service = ObjectProviderUtils.getIfAvailable(myServiceProvider);
     * if (service != null) {
     *     service.doSomething();
     * } else {
     *     System.out.println("MyService is not available.");
     * }
     * }</pre>
     *
     * @param objectProvider the {@link ObjectProvider} to retrieve the object from
     * @param <T>            the type of the object
     * @return an instance of the bean, or {@code null} if not available
     * @throws BeansException if there is an error creating the bean
     * @see ObjectProvider#getObject()
     * @see ObjectProvider#getIfAvailable()
     * @since Spring Framework 4.3
     */
    @Nullable
    public static <T> T getIfAvailable(ObjectProvider<T> objectProvider) {
        return objectProvider == null ? null : objectProvider.getIfAvailable();
    }

    /**
     * Retrieve the object managed by the given {@link ObjectProvider} if available,
     * otherwise obtain the object using the provided default supplier.
     *
     * <p>This method is useful when you want to safely obtain a bean instance
     * and provide a fallback mechanism in case the bean is not available.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * MyService service = ObjectProviderUtils.getIfAvailable(myServiceProvider, () -> new DefaultMyService());
     * service.doSomething();
     * }</pre>
     *
     * @param objectProvider  the {@link ObjectProvider} to retrieve the object from
     * @param defaultSupplier a callback for supplying a default object
     *                        if none is present in the factory
     * @param <T>             the type of the object
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
     * <p>This method safely retrieves the object from the provider and passes it to the
     * consumer if both are non-null. It does not throw an exception if no object is available.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * ObjectProviderUtils.ifAvailable(myServiceProvider, service -> {
     *     service.doSomething();
     * });
     * }</pre>
     *
     * @param objectProvider     the {@link ObjectProvider} to retrieve the object from
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
     * Retrieve the object managed by the given {@link ObjectProvider} if it is unique,
     * without throwing an exception in case of no such bean being present or multiple candidates found.
     *
     * <p>This method is useful when you want to safely obtain a unique bean instance
     * and handle the absence or ambiguity of it gracefully.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * MyService service = ObjectProviderUtils.getIfUnique(myServiceProvider);
     * if (service != null) {
     *     service.doSomething();
     * } else {
     *     System.out.println("MyService is either not available or not unique.");
     * }
     * }</pre>
     *
     * @param objectProvider the {@link ObjectProvider} to retrieve the object from
     * @param <T>            the type of the object
     * @return an instance of the bean if it's uniquely available, or {@code null} if not available or not unique
     * @throws BeansException if there is an error creating the bean
     * @see ObjectProvider#getIfUnique()
     * @since Spring Framework 4.3
     */
    @Nullable
    public static <T> T getIfUnique(@Nullable ObjectProvider<T> objectProvider) throws BeansException {
        return objectProvider == null ? null : objectProvider.getIfUnique();
    }

    /**
     * Retrieve the object managed by the given {@link ObjectProvider} if it is unique,
     * otherwise obtain the object using the provided default supplier.
     *
     * <p>This method is useful when you want to safely obtain a unique bean instance
     * and provide a fallback mechanism in case the bean is either not available or not unique.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * MyService service = ObjectProviderUtils.getIfUnique(myServiceProvider, () -> new DefaultMyService());
     * service.doSomething();
     * }</pre>
     *
     * @param objectProvider  the {@link ObjectProvider} to retrieve the object from
     * @param defaultSupplier a callback for supplying a default object
     *                        if none is present in the factory or not unique
     * @param <T>             the type of the object
     * @return an instance of the bean if it's uniquely available, or the supplied default object
     * if no such bean is available or if multiple candidates exist without a primary one
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
     * Consume the unique object instance managed by the given {@link ObjectProvider} if available.
     *
     * <p>This method safely retrieves the unique object from the provider and passes it to the
     * consumer if both are non-null. It does not throw an exception if no object is available or
     * if multiple candidates exist.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * ObjectProviderUtils.ifUnique(myServiceProvider, service -> {
     *     service.doSomething();
     * });
     * }</pre>
     *
     * @param objectProvider     the {@link ObjectProvider} to retrieve the object from
     * @param dependencyConsumer a callback for processing the target object
     *                           if unique (not called otherwise)
     * @throws BeansException in case of creation errors
     * @see ObjectProvider#getIfUnique()
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
     * Returns an {@link Iterator} over all matching object instances managed by the given
     * {@link ObjectProvider}, typically in registration order.
     *
     * <p>This method is useful when you want to iterate through all available bean instances
     * without using a stream. It provides access to the underlying iterator of the provider.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * Iterator<MyService> serviceIterator = ObjectProviderUtils.iterator(myServiceProvider);
     * while (serviceIterator.hasNext()) {
     *     MyService service = serviceIterator.next();
     *     service.doSomething();
     * }
     * }</pre>
     *
     * @param objectProvider the {@link ObjectProvider} to retrieve the iterator from
     * @param <T>            the type of the objects managed by the provider
     * @return an {@link Iterator} for iterating through the available objects
     * @throws UnsupportedOperationException if the version of Spring Framework is less than 5.1
     *                                       or if the provider does not support iteration
     * @see ObjectProvider#stream()
     * @since Spring Framework 5.1
     */
    @Nonnull
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
     * <p>This method is useful when you want to process all available bean instances
     * in a functional style using streams. It provides a convenient way to access
     * and manipulate the collection of beans managed by the provider.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * List<MyService> services = ObjectProviderUtils.stream(myServiceProvider)
     *                                                .filter(Objects::nonNull)
     *                                               .toList();
     * services.forEach(service -> {
     *     service.doSomething();
     * });
     * }</pre>
     *
     * @param objectProvider the {@link ObjectProvider} to retrieve the stream from
     * @param <T>            the type of the objects managed by the provider
     * @return a sequential {@link Stream} for processing the available objects
     * @throws UnsupportedOperationException if the version of Spring Framework is less than 5.1
     * @see ObjectProvider#iterator()
     * @see ObjectProvider#orderedStream()
     * @since Spring Framework 5.1
     */
    @Nonnull
    public static <T> Stream<T> stream(@Nullable ObjectProvider<T> objectProvider) {
        Iterator<T> iterator = iterator(objectProvider);
        Spliterator<T> spliterator = spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Return a sequential {@link Stream} over all matching object instances,
     * pre-ordered according to the factory's common order comparator.
     *
     * <p>In a standard Spring application context, this will be ordered
     * according to the following rules:
     * <ul>
     *     <li>Beans implementing the {@link Ordered} interface will be sorted based on their order value.</li>
     *     <li>Beans annotated with {@link Order} will be sorted based on the specified order value.</li>
     *     <li>If no explicit order is defined, the default ordering will be used (typically registration order).</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ObjectProvider<MyService> myServiceProvider = context.getBeanProvider(MyService.class);
     * List<MyService> orderedServices = ObjectProviderUtils.orderedStream(myServiceProvider)
     *                                                     .filter(Objects::nonNull)
     *                                                     .toList();
     * orderedServices.forEach(service -> {
     *     service.doSomething();
     * });
     * }</pre>
     *
     * @param objectProvider the {@link ObjectProvider} to retrieve the stream from
     * @param <T>            the type of the objects managed by the provider
     * @return a sequential {@link Stream} for processing the available objects in the correct order
     * @throws UnsupportedOperationException if the version of Spring Framework is less than 5.1
     * @see ObjectProvider#stream()
     * @see OrderComparator
     * @since Spring Framework 5.1
     */
    @Nonnull
    public static <T> Stream<T> orderedStream(@Nullable ObjectProvider<T> objectProvider) {
        return stream(objectProvider).sorted(INSTANCE);
    }

    private ObjectProviderUtils() {
    }
}
