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
import io.microsphere.lang.MutableInteger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.constants.PathConstants.SLASH;
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
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.StringUtils.contains;
import static io.microsphere.util.StringUtils.endsWith;
import static io.microsphere.util.StringUtils.replace;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.startsWith;
import static io.microsphere.util.StringUtils.substringAfter;
import static io.microsphere.util.StringUtils.substringBetween;
import static java.lang.Integer.parseInt;
import static java.lang.reflect.Array.newInstance;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.springframework.http.HttpMethod.resolve;
import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RequestPredicates.pathExtension;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;
import static org.springframework.web.util.UriUtils.extractFileExtension;

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
     * @see RequestPredicate#and(RequestPredicate)
     * @see RequestPredicates.AndRequestPredicate
     */
    AND {

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
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate, "org.springframework.web.reactive.function.server.RequestPredicates$AndRequestPredicate");
        }

        @Override
        public boolean matches(String expression) {
            return startsWith(expression, prefix) && contains(expression, infix) && endsWith(expression, postfix);
        }

        @Override
        public RequestPredicate predicate(String expression) {
            String leftAndRightExp = substringBetween(expression, prefix, postfix);
            String[] leftAndRight = StringUtils.split(leftAndRightExp, infix);
            RequestPredicate left = buildRequestPredicate(leftAndRight[0]);
            RequestPredicate right = buildRequestPredicate(leftAndRight[1]);
            return left.and(right);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            RequestPredicate left = getFieldValue(predicate, "left");
            RequestPredicate right = getFieldValue(predicate, "right");
            return prefix + toExpression(left) + infix + toExpression(right) + postfix;
        }
    },

    /**
     * @see RequestPredicate#or(RequestPredicate)
     * @see RequestPredicates.OrRequestPredicate
     */
    OR {

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
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate, "org.springframework.web.reactive.function.server.RequestPredicates$OrRequestPredicate");
        }

        @Override
        public boolean matches(String expression) {
            return startsWith(expression, prefix) && contains(expression, infix) && endsWith(expression, postfix);
        }

        @Override
        public RequestPredicate predicate(String expression) {
            String leftAndRightExp = substringBetween(expression, prefix, postfix);
            String[] leftAndRight = split(leftAndRightExp, infix);
            RequestPredicate left = buildRequestPredicate(leftAndRight[0]);
            RequestPredicate right = buildRequestPredicate(leftAndRight[1]);
            return left.or(right);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            RequestPredicate left = getFieldValue(predicate, "left");
            RequestPredicate right = getFieldValue(predicate, "right");
            String expression = LEFT_PARENTHESIS + toExpression(left) + infix + toExpression(right) + RIGHT_PARENTHESIS;
            return expression;
        }
    },

    /**
     * @see RequestPredicate#negate()
     */
    NEGATE {

        private static final String prefix = EXCLAMATION;

        private final String implementationClassName = all().negate().getClass().getName();

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            RequestPredicate delegate = getDelegate(predicate);
            visitor.startNegate();
            acceptVisitor(delegate, visitor);
            visitor.endNegate();
        }

        @Override
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate, implementationClassName);
        }

        @Override
        public boolean matches(String expression) {
            return startsWith(expression, prefix);
        }

        @Override
        public RequestPredicate predicate(String expression) {
            String exp = expression.substring(prefix.length());
            RequestPredicate delegate = buildRequestPredicate(exp);
            return delegate.negate();
        }

        @Override
        public String expression(RequestPredicate predicate) {
            RequestPredicate delegate = getDelegate(predicate);
            return prefix + toExpression(delegate);
        }

        RequestPredicate getDelegate(RequestPredicate requestPredicate) {
            RequestPredicate delegate = getFieldValue(requestPredicate, "arg$1");
            return delegate == null ? getFieldValue(requestPredicate, "delegate") : delegate;
        }
    },

    /**
     * @see RequestPredicates#method(HttpMethod)
     * @see RequestPredicates.HttpMethodPredicate
     */
    METHOD {
        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String method = expression(predicate);
            HttpMethod httpMethod = resolve(method);
            visitor.method(ofSet(httpMethod));
        }

        @Override
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate, "org.springframework.web.reactive.function.server.RequestPredicates$HttpMethodPredicate");
        }

        @Override
        public boolean matches(String expression) {
            return resolve(expression) != null;
        }

        @Override
        public RequestPredicate predicate(String expression) {
            HttpMethod httpMethod = resolve(expression);
            return method(httpMethod);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            return predicate.toString();
        }
    },

    /**
     * @see RequestPredicates#path(String)
     * @see RequestPredicates.PathPatternPredicate
     */
    PATH {

        private final String implementationClassName = path(SLASH).getClass().getName();

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String patternString = expression(predicate);
            visitor.path(patternString);
        }

        @Override
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate, implementationClassName);
        }

        @Override
        public boolean matches(String expression) {
            return false;
        }

        @Override
        public RequestPredicate predicate(String expression) {
            return path(expression);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            return predicate.toString();
        }
    },

    /**
     * @see RequestPredicates#pathExtension(String)
     */
    PATH_EXTENSION {
        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String expression = expression(predicate);
            String extension = getExtension(expression);
            visitor.pathExtension(extension);
        }

        @Override
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate, "org.springframework.web.reactive.function.server.RequestPredicates$PathExtensionPredicate");
        }

        @Override
        public boolean matches(String expression) {
            return getExtension(expression) != null;
        }

        @Override
        public RequestPredicate predicate(String expression) {
            String extension = getExtension(expression);
            return pathExtension(extension);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            return "";
        }

        String getExtension(String expression) {
            return extractFileExtension(expression);
        }
    },

    /**
     * @see RequestPredicates#queryParam(String, String)
     * @see RequestPredicates.QueryParamPredicate
     */
    QUERY_PARAM {

        private static final String prefix = QUESTION_MARK;

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String expression = expression(predicate);
            String[] nameAndValue = getNameAndValue(expression);
            String name = nameAndValue[0];
            String value = nameAndValue[1];
            visitor.queryParam(name, value);
        }

        @Override
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate, "org.springframework.web.reactive.function.server.RequestPredicates$QueryParamPredicate");
        }

        @Override
        public boolean matches(String expression) {
            return startsWith(expression, prefix) && contains(expression, SPACE);
        }

        @Override
        public RequestPredicate predicate(String expression) {
            String[] nameAndValue = getNameAndValue(normalize(expression));
            String name = nameAndValue[0];
            String value = nameAndValue[1];
            return queryParam(name, value);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            return normalize(predicate.toString());
        }

        /**
         * Normalize the expression in order to compatible with Spring WebFlux 5.0
         * @param expression
         * @return
         */
        String normalize(String expression) {
            return replace(expression, " == ", SPACE);
        }

        String[] getNameAndValue(String expression) {
            String nameAndValue = expression.substring(prefix.length());
            return split(nameAndValue, SPACE);
        }
    },

    /**
     * @see RequestPredicates#accept(MediaType...)
     */
    ACCEPT {

        private static final String prefix = "Accept: ";

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String expression = expression(predicate);
            String mediaTypesAsString = parseMediaTypesString(expression);
            visitor.header(HttpHeaders.ACCEPT, mediaTypesAsString);
        }

        @Override
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate.toString());
        }

        @Override
        public boolean matches(String expression) {
            return startsWith(expression, prefix);
        }

        @Override
        public RequestPredicate predicate(String expression) {
            Set<MediaType> mediaTypes = mediaTypes(expression);
            MediaType[] mediaTypesArray = mediaTypes.stream().toArray(MediaType[]::new);
            return RequestPredicates.accept(mediaTypesArray);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            String expression = predicate.toString();
            Set<MediaType> mediaTypes = mediaTypes(expression);
            String mediaTypesAsString = toString(mediaTypes);
            return prefix + mediaTypesAsString;
        }

        Set<MediaType> mediaTypes(String expression) {
            String mediaTypesAsString = parseMediaTypesString(expression);
            return resolveValues(mediaTypesAsString, MediaType::parseMediaType);
        }

        String parseMediaTypesString(String expression) {
            return expression.substring(prefix.length());
        }
    },

    /**
     * @see RequestPredicates#contentType(MediaType...)
     */
    CONTENT_TYPE {

        private static final String prefix = "Content-Type: ";

        @Override
        void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
            String expression = expression(predicate);
            String mediaTypesAsString = parseMediaTypesString(expression);
            visitor.header(HttpHeaders.CONTENT_TYPE, mediaTypesAsString);
        }

        @Override
        public boolean matches(RequestPredicate predicate) {
            return matches(predicate.toString());
        }

        @Override
        public boolean matches(String expression) {
            return startsWith(expression, prefix);
        }

        @Override
        public RequestPredicate predicate(String expression) {
            Set<MediaType> mediaTypes = mediaTypes(expression);
            MediaType[] mediaTypesArray = mediaTypes.stream().toArray(MediaType[]::new);
            return contentType(mediaTypesArray);
        }

        @Override
        public String expression(RequestPredicate predicate) {
            String expression = predicate.toString();
            Set<MediaType> mediaTypes = mediaTypes(expression);
            String mediaTypesAsString = toString(mediaTypes);
            return prefix + mediaTypesAsString;
        }

        Set<MediaType> mediaTypes(String expression) {
            String mediaTypesAsString = parseMediaTypesString(expression);
            return resolveValues(mediaTypesAsString, MediaType::parseMediaType);
        }

        String parseMediaTypesString(String expression) {
            return expression.substring(prefix.length());
        }
    },

    /**
     * @see RequestPredicates#headers(Predicate)
     * @see RequestPredicates.HeadersPredicate
     */
    HEADERS {

        private static final String CLASS_NAME = "org.springframework.web.reactive.function.server.RequestPredicates$HeadersPredicate";

        @Override
        public boolean matches(RequestPredicate predicate) {
            String className = getTypeName(predicate);
            return CLASS_NAME.equals(className);
        }

        @Override
        public boolean matches(String expression) {
            return startsWith(expression, CLASS_NAME);
        }

        @Override
        public RequestPredicate predicate(String expression) {
            return t -> false;
        }

        @Override
        public String expression(RequestPredicate predicate) {
            return getTypeName(predicate) + "@" + predicate.hashCode();
        }

    },

    /**
     * Unknown
     */
    UNKNOWN {
        @Override
        public boolean matches(RequestPredicate predicate) {
            return false;
        }

        @Override
        public boolean matches(String expression) {
            return false;
        }

        @Override
        public RequestPredicate predicate(String expression) {
            return t -> false;
        }

        @Override
        public String expression(RequestPredicate predicate) {
            return predicate.toString();
        }
    };

    private static ThreadLocal<List<RequestPredicate>> requestPredicateThreadLocal = new ThreadLocal<>();

    /**
     * Accepts the given {@link RequestPredicate}
     *
     * @param predicate the {@link RequestPredicate}
     * @param visitor   the {@link RequestPredicateVisitorAdapter}
     */
    void accept(RequestPredicate predicate, RequestPredicateVisitorAdapter visitor) {
        visitor.unknown(predicate);
    }

    /**
     * Determines whether the given {@link RequestPredicate} matches the current {@link RequestPredicateKind}
     *
     * @param predicate the {@link RequestPredicate}
     * @return <code>true</code> if matches, or <code>false</code>
     */
    public abstract boolean matches(RequestPredicate predicate);

    /**
     * Determines whether the given {@code expression} matches the current {@link RequestPredicateKind}
     *
     * @param expression the expression, like : "GET"
     * @return <code>true</code> if matches, or <code>false</code>
     */
    public abstract boolean matches(@Nullable String expression);

    /**
     * Builds a {@link RequestPredicate} from the given {@code expression}
     *
     * @param expression the expression, like : "GET"
     * @return the {@link RequestPredicate} if matches, or <code>null</code>
     */
    public abstract RequestPredicate predicate(@Nullable String expression);

    /**
     * Generates an expression string representation of the given {@link RequestPredicate}.
     * <p>
     * This method provides a way to serialize a {@link RequestPredicate} back into a string
     * expression that can be used to recreate the same predicate using {@link #parseRequestPredicate(String)}.
     * </p>
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>"GET" - represents a method predicate for GET requests</li>
     *   <li>"/users" - represents a path predicate for /users</li>
     *   <li>"*.json" - represents a path extension predicate for .json files</li>
     *   <li>"?name=value" - represents a query parameter predicate</li>
     *   <li>"Accept: application/json" - represents an accept header predicate</li>
     *   <li>"Content-Type: application/json" - represents a content type header predicate</li>
     *   <li>"(GET && /users)" - represents a compound AND predicate</li>
     *   <li>"(GET || POST)" - represents a compound OR predicate</li>
     *   <li>"!GET" - represents a negated predicate</li>
     * </ul>
     *
     * @param predicate the {@link RequestPredicate} to convert to an expression
     * @return a string expression representing the {@link RequestPredicate}
     * @throws NullPointerException if the {@code requestPredicate} is {@code null}
     * @see #parseRequestPredicate(String)
     */
    public abstract String expression(RequestPredicate predicate);

    static boolean matches(RequestPredicate predicate, String className) {
        return Objects.equals(getTypeName(predicate), className);
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
     *   <li>"!GET" - matches all requests except GET</li>
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
     * Converts the given {@link RequestPredicate} into its string expression representation.
     * <p>
     * This method is the inverse of {@link #parseRequestPredicate(String)}. It takes a {@link RequestPredicate}
     * and generates a string expression that represents the predicate. The generated expression can be used
     * to recreate the same predicate using {@link #parseRequestPredicate(String)}.
     * </p>
     * <h3>Example Usage</h3>
     * <ul>
     *   <li>"GET" - represents a method predicate for GET requests</li>
     *   <li>"/users" - represents a path predicate for /users</li>
     *   <li>"*.json" - represents a path extension predicate for .json files</li>
     *   <li>"?name=value" - represents a query parameter predicate</li>
     *   <li>"Accept: application/json" - represents an accept header predicate</li>
     *   <li>"Content-Type: application/json" - represents a content type header predicate</li>
     *   <li>"(GET && /users)" - represents a compound AND predicate</li>
     *   <li>"(GET || POST)" - represents a compound OR predicate</li>
     *   <li>"!GET" - represents a negated predicate</li>
     * </ul>
     *
     * @param predicate the {@link RequestPredicate} to convert to an expression
     * @return a string expression representing the {@link RequestPredicate}
     * @throws NullPointerException if the {@code predicate} is {@code null}
     * @see #parseRequestPredicate(String)
     * @see #expression(RequestPredicate)
     */
    public static String toExpression(RequestPredicate predicate) {
        RequestPredicateKind kind = valueOf(predicate);
        return kind.expression(predicate);
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
        for (RequestPredicateKind kind : values()) {
            if (kind.matches(predicate)) {
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

    <V> Set<V> resolveValues(String expression, Function<String, V> valueFunction) {
        String values = substringBetween(expression, LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET);
        String[] elements = values == null ? ofArray(expression) : split(values, COMMA);
        return of(elements)
                .filter(Objects::nonNull)
                .map(valueFunction::apply)
                .filter(Objects::nonNull)
                .collect(toSet());
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
