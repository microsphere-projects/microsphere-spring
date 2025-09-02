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

import io.microsphere.annotation.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.acceptVisitor;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * The adapter interface for {@link RequestPredicates.Visitor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestPredicates.Visitor
 * @see RequestPredicate
 * @see RequestPredicates
 * @since 1.0.0
 */
public interface RequestPredicateVisitorAdapter extends InvocationHandler {

    /**
     * The {@link Class} of {@link RequestPredicates.Visitor}
     *
     * @see RequestPredicates.Visitor
     * @since Spring Framework 5.1
     */
    @Nullable
    Class<?> VISITOR_CLASS = resolveClass("org.springframework.web.reactive.function.server.RequestPredicates$Visitor",
            RequestPredicates.class.getClassLoader());

    /**
     * The {@link Method} of {@link RequestPredicate#accept(RequestPredicates.Visitor)}
     *
     * @since Spring Framework 5.1
     */
    @Nullable
    Method ACCEPT_METHOD = VISITOR_CLASS == null ? null : findMethod(RequestPredicate.class, "accept", VISITOR_CLASS);

    /**
     * Receive notification of an HTTP method predicate.
     *
     * @param methods the HTTP methods that make up the predicate
     * @see RequestPredicates#method(HttpMethod)
     * @see RequestPredicates.Visitor#method(Set)
     */
    default void method(Set<HttpMethod> methods) {
    }

    /**
     * Receive notification of a path predicate.
     *
     * @param pattern the path pattern that makes up the predicate
     * @see RequestPredicates#path(String)
     * @see RequestPredicates.Visitor#path(String)
     */
    default void path(String pattern) {
    }

    /**
     * Receive notification of a path extension predicate.
     *
     * @param extension the path extension that makes up the predicate
     * @see RequestPredicates#pathExtension(String)
     * @see RequestPredicates.Visitor#pathExtension(String)
     */
    default void pathExtension(String extension) {
    }

    /**
     * Receive notification of an HTTP header predicate.
     *
     * @param name  the name of the HTTP header to check
     * @param value the desired value of the HTTP header
     * @see RequestPredicates#headers(Predicate)
     * @see RequestPredicates#contentType(MediaType...)
     * @see RequestPredicates#accept(MediaType...)
     * @see RequestPredicates.Visitor#header(String, String)
     */
    default void header(String name, String value) {
    }

    /**
     * Receive notification of a query parameter predicate.
     *
     * @param name  the name of the query parameter
     * @param value the desired value of the parameter
     * @see RequestPredicates#queryParam(String, String)
     * @see RequestPredicates.Visitor#queryParam(String, String)
     */
    default void queryParam(String name, String value) {
    }

    /**
     * Receive first notification of a logical AND predicate.
     * The first subsequent notification will contain the left-hand side of the AND-predicate;
     * followed by {@link #and()}, followed by the right-hand side, followed by {@link #endAnd()}.
     *
     * @see RequestPredicate#and(RequestPredicate)
     * @see RequestPredicates.Visitor#startAnd()
     */
    default void startAnd() {
    }

    /**
     * Receive "middle" notification of a logical AND predicate.
     * The following notification contains the right-hand side, followed by {@link #endAnd()}.
     *
     * @see RequestPredicate#and(RequestPredicate)
     * @see RequestPredicates.Visitor#and()
     */
    default void and() {
    }

    /**
     * Receive last notification of a logical AND predicate.
     *
     * @see RequestPredicate#and(RequestPredicate)
     * @see RequestPredicates.Visitor#endAnd()
     */
    default void endAnd() {
    }

    /**
     * Receive first notification of a logical OR predicate.
     * The first subsequent notification will contain the left-hand side of the OR-predicate;
     * the second notification contains the right-hand side, followed by {@link #endOr()}.
     *
     * @see RequestPredicate#or(RequestPredicate)
     * @see RequestPredicates.Visitor#startOr()
     */
    default void startOr() {
    }

    /**
     * Receive "middle" notification of a logical OR predicate.
     * The following notification contains the right-hand side, followed by {@link #endOr()}.
     *
     * @see RequestPredicate#or(RequestPredicate)
     * @see RequestPredicates.Visitor#or()
     */
    default void or() {
    }

    /**
     * Receive last notification of a logical OR predicate.
     *
     * @see RequestPredicate#or(RequestPredicate)
     * @see RequestPredicates.Visitor#endOr()
     */
    default void endOr() {
    }

    /**
     * Receive first notification of a negated predicate.
     * The first subsequent notification will contain the negated predicated, followed
     * by {@link #endNegate()}.
     *
     * @see RequestPredicate#negate()
     * @see RequestPredicates.Visitor#startNegate()
     */
    default void startNegate() {
    }

    /**
     * Receive last notification of a negated predicate.
     *
     * @see RequestPredicate#negate()
     * @see RequestPredicates.Visitor#endNegate()
     */
    default void endNegate() {
    }

    /**
     * Receive first notification of an unknown predicate.
     *
     * @see RequestPredicates.Visitor#unknown(RequestPredicate)
     */
    default void unknown(RequestPredicate predicate) {
    }

    default boolean isVisitorSupported() {
        return VISITOR_CLASS != null;
    }

    /**
     * Get the proxy of {@link RequestPredicates.Visitor} since Spring 5.1
     *
     * @return <code>null</code> if {@link RequestPredicates.Visitor} is absent
     */
    default Object getProxy() {
        ClassLoader classLoader = getClassLoader(getClass());
        return newProxyInstance(classLoader, ofArray(VISITOR_CLASS), this);
    }

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Method delegateMethod = findMethod(RequestPredicateVisitorAdapter.class, methodName, parameterTypes);
        return delegateMethod.invoke(this, args);
    }

    /**
     * Invoke {@link RequestPredicate#accept(RequestPredicates.Visitor)} in Java Reflection if
     * {@link #isVisitorSupported() the RequestPredicates.Visitor is supported}, otherwise invoke
     * {@link RequestPredicateKind#acceptVisitor(RequestPredicate, RequestPredicateVisitorAdapter)}
     *
     * @param predicate {@link RequestPredicate}
     * @see RequestPredicate#accept(RequestPredicates.Visitor)
     * @see RequestPredicateKind#acceptVisitor(RequestPredicate, RequestPredicateVisitorAdapter)
     */
    default void visit(RequestPredicate predicate) {
        if (isVisitorSupported()) {
            Object visitor = getProxy();
            invokeMethod(predicate, ACCEPT_METHOD, visitor);
        } else {
            acceptVisitor(predicate, this);
        }
    }
}
