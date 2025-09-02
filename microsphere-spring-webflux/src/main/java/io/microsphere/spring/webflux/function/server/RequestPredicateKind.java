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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.MutableInteger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.DOLLAR;
import static io.microsphere.constants.SymbolConstants.EXCLAMATION;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET;
import static io.microsphere.constants.SymbolConstants.QUESTION_MARK;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET;
import static io.microsphere.constants.SymbolConstants.SPACE;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.contains;
import static io.microsphere.util.StringUtils.endsWith;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.startsWith;
import static io.microsphere.util.StringUtils.substringAfter;
import static io.microsphere.util.StringUtils.substringBetween;
import static java.lang.Integer.parseInt;
import static java.lang.reflect.Array.newInstance;
import static java.util.stream.Stream.of;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RequestPredicates.pathExtension;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

/**
 * The kind of {@link RequestPredicate} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestPredicates
 * @see RequestPredicates#path(String)
 * @see RequestPredicates#pathExtension(String)
 * @see RequestPredicates#method(HttpMethod)
 * @see RequestPredicates#queryParam(String, String)
 * @see RequestPredicates#accept(MediaType...)
 * @see RequestPredicates#contentType(MediaType...)
 * @see RequestPredicate#and(RequestPredicate)
 * @see RequestPredicate#or(RequestPredicate)
 * @see RequestPredicate#negate()
 * @see RequestPredicate#nest(ServerRequest)
 * @see RequestPredicate
 * @since 1.0.0
 */
public enum RequestPredicateKind {

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.HttpMethodPredicate
     */
    METHOD("org.springframework.web.reactive.function.server.RequestPredicates.HttpMethodPredicate") {
        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            Set<HttpMethod> httpMethods = getFieldValue(predicate, "httpMethods");
            visitor.method(httpMethods);
        }

        @Override
        boolean matches(String expression) {
            HttpMethod[] httpMethods = resolveMethods(expression);
            return isNotEmpty(httpMethods);
        }

        @Override
        RequestPredicate predicate(String expression) {
            HttpMethod[] httpMethods = resolveMethods(expression);
            RequestPredicate methodsPredicate = null;
            for (HttpMethod httpMethod : httpMethods) {
                RequestPredicate predicate = method(httpMethod);
                if (methodsPredicate == null) {
                    methodsPredicate = predicate;
                } else {
                    methodsPredicate = methodsPredicate.and(predicate);
                }
            }
            return methodsPredicate;
        }

        protected HttpMethod[] resolveMethods(String expression) {
            return resolveValues(expression, HttpMethod.class, HttpMethod::resolve);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.PathPatternPredicate
     */
    PATH("org.springframework.web.reactive.function.server.RequestPredicates.PathPatternPredicate") {
        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            PathPattern pattern = getFieldValue(predicate, "pattern");
            visitor.path(pattern.getPatternString());
        }

        @Override
        boolean matches(String expression) {
            return super.matches(expression);
        }

        @Override
        RequestPredicate predicate(String expression) {
            return path(expression);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.PathExtensionPredicate
     */
    PATH_EXTENSION("org.springframework.web.reactive.function.server.RequestPredicates.PathExtensionPredicate") {

        private static final String prefix = "*.";

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String extension = getFieldValue(predicate, "extension");
            if (extension == null) {
                Predicate<String> extensionPredicate = getFieldValue(predicate, "extensionPredicate");
                visitor.pathExtension(extensionPredicate.toString());
            } else {
                visitor.pathExtension(extension);
            }
        }

        @Override
        boolean matches(String expression) {
            return startsWith(expression, prefix);
        }

        @Override
        RequestPredicate predicate(String expression) {
            String exp = expression.substring(prefix.length());
            return pathExtension(exp);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.HeadersPredicate
     */
    HEADERS("org.springframework.web.reactive.function.server.RequestPredicates.HeadersPredicate") {
        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            visitor.unknown(predicate);
        }

        @Override
        boolean matches(String expression) {
            return contains(expression, "$$Lambda/");
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.QueryParamPredicate
     */
    QUERY_PARAM("org.springframework.web.reactive.function.server.RequestPredicates.QueryParamPredicate") {

        private static final String prefix = QUESTION_MARK;

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String name = getFieldValue(predicate, "name");
            String value = getFieldValue(predicate, "value");
            if (value == null) {
                Predicate<String> valuePredicate = getFieldValue(predicate, "valuePredicate");
                visitor.queryParam(name, valuePredicate.toString());
            } else {
                visitor.queryParam(name, value);
            }
        }

        @Override
        boolean matches(String expression) {
            return startsWith(expression, prefix) && contains(expression, SPACE);
        }

        @Override
        RequestPredicate predicate(String expression) {
            String exp = expression.substring(prefix.length());
            String[] nameAndValue = split(exp, SPACE);
            String name = nameAndValue[0];
            String value = nameAndValue[1];
            return queryParam(name, value);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.AcceptPredicate
     */
    ACCEPT("org.springframework.web.reactive.function.server.RequestPredicates.AcceptPredicate") {

        private static final String prefix = "Accept: ";

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            Set<MediaType> mediaTypes = getFieldValue(predicate, "mediaTypes");
            visitor.header(HttpHeaders.ACCEPT, toString(mediaTypes));
        }

        @Override
        boolean matches(String expression) {
            return startsWith(expression, prefix);
        }

        @Override
        RequestPredicate predicate(String expression) {
            String values = expression.substring(prefix.length());
            MediaType[] mediaTypes = resolveValues(values, MediaType.class, MediaType::parseMediaType);
            return RequestPredicates.accept(mediaTypes);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.ContentTypePredicate
     */
    CONTENT_TYPE("org.springframework.web.reactive.function.server.RequestPredicates.ContentTypePredicate") {

        private static final String prefix = "Content-Type: ";

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            Set<MediaType> mediaTypes = getFieldValue(predicate, "mediaTypes");
            visitor.header(HttpHeaders.CONTENT_TYPE, toString(mediaTypes));
        }

        @Override
        boolean matches(String expression) {
            return startsWith(expression, prefix);
        }

        @Override
        RequestPredicate predicate(String expression) {
            String values = expression.substring(prefix.length());
            MediaType[] mediaTypes = resolveValues(values, MediaType.class, MediaType::parseMediaType);
            return contentType(mediaTypes);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.AndRequestPredicate
     */
    AND("org.springframework.web.reactive.function.server.RequestPredicates.AndRequestPredicate") {

        private static final String prefix = "(";

        private static final String infix = " && ";

        private static final String postfix = ")";

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            RequestPredicate left = getFieldValue(predicate, "left");
            RequestPredicate right = getFieldValue(predicate, "right");

            visitor.startAnd();
            acceptVisitor(left, visitor);
            visitor.and();
            acceptVisitor(right, visitor);
            visitor.endAnd();
        }

        @Override
        boolean matches(String expression) {
            return startsWith(expression, prefix) && contains(expression, infix) && endsWith(expression, postfix);
        }

        @Override
        RequestPredicate predicate(String expression) {
            String leftAndRightExp = substringBetween(expression, prefix, postfix);
            String[] leftAndRight = StringUtils.split(leftAndRightExp, infix);
            RequestPredicate left = buildRequestPredicate(leftAndRight[0]);
            RequestPredicate right = buildRequestPredicate(leftAndRight[1]);
            return left.and(right);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.OrRequestPredicate
     */
    OR("org.springframework.web.reactive.function.server.RequestPredicates.OrRequestPredicate") {

        private static final String prefix = LEFT_PARENTHESIS;

        private static final String infix = " || ";

        private static final String postfix = RIGHT_PARENTHESIS;

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            RequestPredicate left = getFieldValue(predicate, "left");
            RequestPredicate right = getFieldValue(predicate, "right");

            visitor.startOr();
            acceptVisitor(left, visitor);
            visitor.or();
            acceptVisitor(right, visitor);
            visitor.endOr();
        }

        @Override
        boolean matches(String expression) {
            return startsWith(expression, prefix) && contains(expression, infix) && endsWith(expression, postfix);
        }

        @Override
        RequestPredicate predicate(String expression) {
            String leftAndRightExp = substringBetween(expression, prefix, postfix);
            String[] leftAndRight = split(leftAndRightExp, infix);
            RequestPredicate left = buildRequestPredicate(leftAndRight[0]);
            RequestPredicate right = buildRequestPredicate(leftAndRight[1]);
            return left.or(right);
        }
    },

    /**
     * @see org.springframework.web.reactive.function.server.RequestPredicates.NegateRequestPredicate
     */
    NEGATE("org.springframework.web.reactive.function.server.RequestPredicates.NegateRequestPredicate") {

        private static final String prefix = EXCLAMATION;

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            RequestPredicate delegate = getFieldValue(predicate, "delegate");
            visitor.startNegate();
            acceptVisitor(delegate, visitor);
            visitor.endNegate();
        }

        @Override
        boolean matches(String expression) {
            return startsWith(expression, prefix);
        }

        @Override
        RequestPredicate predicate(String expression) {
            String exp = expression.substring(prefix.length());
            RequestPredicate delegate = buildRequestPredicate(exp);
            return delegate.negate();
        }
    },

    /**
     * Unknown
     */
    UNKNOWN("") {
        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            visitor.unknown(predicate);
        }
    };

    private static ThreadLocal<List<RequestPredicate>> requestPredicateThreadLocal = new ThreadLocal<>();

    private final String implementationClassName;

    RequestPredicateKind(String implementationClassName) {
        this.implementationClassName = implementationClassName;
    }

    /**
     * Gets the implementation class name
     *
     * @return the implementation class name
     */
    @Nonnull
    public final String getImplementationClassName() {
        return implementationClassName;
    }

    /**
     * Accepts the given {@link RequestPredicate}
     *
     * @param predicate the {@link RequestPredicate}
     * @param visitor   the {@link RequestPredicateVisitorAdapter}
     */
    abstract void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor);

    /**
     * Determines whether the given {@code expression} matches the current {@link RequestPredicateKind}
     *
     * @param expression the expression, like : "GET"
     * @return <code>true</code> if matches, or <code>false</code>
     */
    boolean matches(@Nullable String expression) {
        return false;
    }

    /**
     * Builds a {@link RequestPredicate} from the given {@code expression}
     *
     * @param expression the expression, like : "GET"
     * @return the {@link RequestPredicate} if matches, or <code>null</code>
     */
    RequestPredicate predicate(@Nullable String expression) {
        return null;
    }

    /**
     * Accepts all the given {@link RequestPredicate}
     *
     * @param predicate the {@link RequestPredicate}
     * @param visitor   the {@link RequestPredicateVisitorAdapter}
     */
    static void acceptVisitor(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
        RequestPredicateKind kind = valueOf(predicate);
        kind.accept(predicate, visitor);
    }

    /**
     * Parses the given expression into a {@link RequestPredicate}.
     * <p>
     * The expression can be a simple predicate like "GET" or a complex one like "(GET && /users)".
     * </p>
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>"GET" - matches GET requests</li>
     *   <li>"*.json" - matches requests with .json extension</li>
     *   <li>"?name=value" - matches requests with query parameter name=value</li>
     *   <li>"Accept: application/json" - matches requests accepting JSON</li>
     *   <li>"Content-Type: application/json" - matches requests with JSON content type</li>
     *   <li>"(GET && /users)" - matches GET requests to /users</li>
     *   <li>"(GET || POST)" - matches either GET or POST requests</li>
     *   <li>"!(GET)" - matches all requests except GET</li>
     * </ul>
     * </p>
     *
     * @param expression the expression to parse
     * @return the parsed {@link RequestPredicate}
     * @throws IllegalArgumentException if the expression is invalid
     */
    public static RequestPredicate parseRequestPredicate(String expression) {
        StringBuilder exp = new StringBuilder(expression);
        List<String> composeExpressions = getComposeExpressions(exp);

        int size = composeExpressions.size();
        if (size == 0) {
            return buildRequestPredicate(expression);
        } else {
            List<RequestPredicate> requestPredicates = newArrayList(size);
            // set the requestPredicates
            requestPredicateThreadLocal.set(requestPredicates);
            for (int i = 0; i < size; i++) {
                String composeExpression = composeExpressions.get(i);
                RequestPredicate predicate = buildRequestPredicate(composeExpression);
                requestPredicates.add(predicate);
            }
            // clear the requestPredicates
            requestPredicateThreadLocal.remove();

            RequestPredicate predicate = requestPredicates.get(size - 1);
            return NEGATE.matches(expression) ? predicate.negate() : predicate;
        }
    }

    /**
     * Resolves the {@link RequestPredicateKind} from the given {@link RequestPredicate}
     *
     * @param predicate the instance of {@link RequestPredicate}
     * @return the {@link RequestPredicateKind} enum constant, if not found, return {@link #UNKNOWN}
     * @throws NullPointerException if the {@code predicate} is {@code null}
     * @see RequestPredicate
     */
    public static RequestPredicateKind valueOf(RequestPredicate predicate) {
        String className = predicate.getClass().getCanonicalName();
        for (RequestPredicateKind kind : values()) {
            if (kind.getImplementationClassName().equals(className)) {
                return kind;
            }
        }
        return UNKNOWN;
    }

    static RequestPredicate buildRequestPredicate(@Nullable String expression) {
        if (startsWith(expression, DOLLAR)) {
            String index = substringAfter(expression, DOLLAR);
            List<RequestPredicate> requestPredicates = requestPredicateThreadLocal.get();
            RequestPredicate requestPredicate = requestPredicates.get(parseInt(index));
            return requestPredicate;
        }
        for (RequestPredicateKind kind : values()) {
            if (kind.matches(expression)) {
                return kind.predicate(expression);
            }
        }
        return PATH.predicate(expression);
    }

    static List<String> getComposeExpressions(StringBuilder expression) {
        List<String> composeExpressions = newLinkedList();
        MutableInteger index = new MutableInteger(0);
        String composeExpression;
        do {
            composeExpression = getComposeExpression(expression, index);
            if (composeExpression == null) {
                break;
            }
            composeExpressions.add(composeExpression);
        } while (true);
        return composeExpressions;
    }

    static String getComposeExpression(StringBuilder expression, MutableInteger index) {
        int endIndex = expression.indexOf(RIGHT_PARENTHESIS);
        if (endIndex == -1) {
            return null;
        }
        int startIndex = -1;
        for (int i = endIndex - 1; i >= 0; i--) {
            if (expression.charAt(i) == LEFT_PARENTHESIS_CHAR) {
                startIndex = i;
                break;
            }
        }
        if (startIndex == -1) {
            return null;
        }
        String composeExpression = expression.substring(startIndex, endIndex + 1);
        // replace the placeholder($index) to the compose expression
        expression.replace(startIndex, endIndex + 1, DOLLAR + index.getAndIncrement());
        return composeExpression;
    }

    static String toString(Collection<?> values) {
        return values.size() == 1 ? values.iterator().next().toString() : values.toString();
    }

    <V> V[] resolveValues(String expression, Class<V> valueType, Function<String, V> valueFunction) {
        String values = substringBetween(expression, LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET);
        String[] elements = values == null ? ofArray(expression) : split(values, COMMA);
        return of(elements)
                .filter(Objects::nonNull)
                .map(valueFunction::apply)
                .filter(Objects::nonNull)
                .toArray(length -> (V[]) newInstance(valueType, length));
    }
}
