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
package io.microsphere.spring.web.metadata;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.json.JSONUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.EQUAL_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE;
import static io.microsphere.constants.SymbolConstants.RIGHT_CURLY_BRACE;
import static io.microsphere.net.URLUtils.buildURI;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.CUSTOMIZED;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.FILTER;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.SERVLET;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_FLUX;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayEquals;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.Assert.assertTrue;
import static io.microsphere.util.IterableUtils.iterate;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static java.util.function.Function.identity;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * The meta-data class for Web Endpoint Mapping that could be one of these endpoints:
 * <ul>
 *     <li>{@link javax.servlet.Servlet Servlet}</li>
 *     <li>{@link javax.servlet.Filter Servlet's Filter}</li>
 *     <li>Spring WebMVC {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}</li>
 *     <li>Spring WebFlux {@link org.springframework.web.reactive.DispatcherHandler DispatcherHandler}</li>
 *     <li>Customized</li>
 * </ul>
 * <p>
 * The method {@link #getKind()} can be used to identify the kind of endpoints, and the method
 * {@link #getEndpoint()} is an abstract presentation of actual endpoint that may be :
 * <ul>
 *     <li>{@link javax.servlet.ServletRegistration#getName() the name of Servlet}</li>
 *     <li>{@link javax.servlet.FilterRegistration#getName() the name of Servlet's Filter}</li>
 *     <li>the any handler of Spring WebMVC {@link org.springframework.web.servlet.HandlerMapping}:
 *      <ul>
 *          <li>The {@link String} presenting the name of Handler bean</li>
 *          <li>The {@link org.springframework.web.servlet.mvc.Controller} Bean</li>
 *          <li>The {@link HandlerMethod} could be annotated the {@link RequestMapping @RequestMapping}</li>
 *          <li>The {@link org.springframework.web.servlet.function.HandlerFunction} since Spring Framework 5.2</li>
 *      </ul>
 *     </li>
 *     <li>the any handler of Spring WebFlux {@link org.springframework.web.reactive.HandlerMapping}:
 *      <ul>
 *          <li>The {@link String} presenting the name of Handler bean</li>
 *          <li>The {@link HandlerMethod} could be annotated the {@link RequestMapping @RequestMapping}</li>
 *          <li>The {@link org.springframework.web.reactive.function.server.HandlerFunction} since Spring Framework 5.0</li>
 *      </ul>
 *     </li>
 * </ul>
 * <p>
 * The method {@link #getSource()} can trace the source of {@link WebEndpointMapping} if present, it could be :
 * <ul>
 *     <li>{@link javax.servlet.ServletContext ServletContext}</li>
 *     <li>Spring WebMVC {@link org.springframework.web.servlet.HandlerMapping}</li>
 *     <li>Spring WebFlux {@link org.springframework.web.reactive.HandlerMapping}</li>
 * </ul>, or it's {@link #UNKNOWN_SOURCE non-source}
 *
 * @param <E> the type of endpoint
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see javax.servlet.ServletRegistration
 * @see javax.servlet.FilterRegistration
 * @see javax.servlet.annotation.WebServlet
 * @see javax.servlet.annotation.WebFilter
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.reactive.DispatcherHandler
 * @see org.springframework.web.servlet.HandlerMapping
 * @see org.springframework.web.reactive.HandlerMapping
 * @see RequestMapping
 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfo
 * @see org.springframework.web.reactive.result.method.RequestMappingInfo
 * @since 1.0.0
 */
public class WebEndpointMapping<E> {

    /**
     * The HTTP header name for {@link WebEndpointMapping#getId()}
     */
    public static final String ID_HEADER_NAME = "microsphere_wem_id";

    /**
     * The source is unknown
     */
    public static final Object UNKNOWN_SOURCE = new Object();

    @Nonnull
    private final Kind kind;

    @Nonnull
    private final transient E endpoint;

    @Nonnull
    private final int id;

    @Nonnull
    private final String[] patterns;

    @Nonnull
    private final String[] methods;

    @Nullable
    private final String[] params;

    @Nullable
    private final String[] headers;

    @Nullable
    private final String[] consumes;

    @Nullable
    private final String[] produces;

    @Nullable
    private transient final Object source;

    private transient int hashCode = 0;

    @Nullable
    private transient Map<String, Object> attributes;

    /**
     * {@link WebEndpointMapping} Kind
     */
    public enum Kind {

        /**
         * {@link javax.servlet.Servlet}
         */
        SERVLET,

        /**
         * {@link javax.servlet.Filter}
         */
        FILTER,

        /**
         * Spring WebMVC {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}
         */
        WEB_MVC,

        /**
         * Spring WebFlux {@link org.springframework.web.reactive.DispatcherHandler DispatcherHandler}
         */
        WEB_FLUX,

        /**
         * Customized
         */
        CUSTOMIZED,

    }

    public static class Builder<E> {

        @Nonnull
        private final Kind kind;

        @Nonnull
        private E endpoint;

        @Nullable
        private Object source;

        @Nonnull
        private Set<String> patterns;

        @Nonnull
        private Set<String> methods;

        @Nullable
        private Set<String> params;

        @Nullable
        private Set<String> headers;

        @Nullable
        private Set<String> consumes;

        @Nullable
        private Set<String> produces;

        /**
         * Create a new {@link Builder} instance.
         *
         * @param kind the {@link Kind} of the endpoint
         * @throws IllegalArgumentException if the {@code kind} or {@code endpoint} is {@code null}
         */
        private Builder(Kind kind) throws IllegalArgumentException {
            assertNotNull(kind, () -> "The 'kind' must not be null");
            this.kind = kind;
        }

        /**
         * Set the endpoint for the WebEndpointMapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.endpoint("myServlet");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.endpoint(handlerMethod);
         * }</pre>
         *
         * @param endpoint the endpoint (must not be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the endpoint is null
         */
        public Builder<E> endpoint(@Nonnull E endpoint) {
            assertNotNull(endpoint, () -> "The 'endpoint' must not be null");
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Add a single path pattern to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.pattern("/api/users");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.pattern("/api/products/{id}");
         * }</pre>
         *
         * @param pattern the path pattern to add (must not be null or blank)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the pattern is null or blank
         */
        @Nonnull
        public Builder<E> pattern(@Nonnull String pattern) throws IllegalArgumentException {
            assertNotNull(pattern, () -> "The 'pattern' must not be null");
            assertNotBlank(pattern, () -> "The 'pattern' must not be blank");
            if (this.patterns == null) {
                this.patterns = newSet();
            }
            this.patterns.add(pattern);
            return this;
        }

        /**
         * Set multiple path patterns to the endpoint mapping from a collection of values.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint with a list of pattern strings
         * List<String> patternList = Arrays.asList("/api/users", "/api/orders");
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.patterns(patternList, Function.identity());
         *
         * // For a WebFlux endpoint with custom objects that have a getName() method
         * List<MyPathPattern> patterns = getCustomPatternObjects();
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.patterns(patterns, MyPathPattern::getName);
         * }</pre>
         *
         * @param values         the collection of values to convert to patterns (must not be null)
         * @param stringFunction the function to convert each value to a string pattern (must not be null)
         * @param <V>            the type of values in the collection
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the values or stringFunction is null
         */
        @Nonnull
        public <V> Builder<E> patterns(Collection<V> values, Function<V, String> stringFunction) {
            return patterns(toStrings(values, stringFunction));
        }

        /**
         * Set multiple path patterns to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.patterns("/api/users", "/api/orders");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.patterns("/api/products/{id}", "/api/categories/{categoryId}");
         * }</pre>
         *
         * @param patterns the path patterns to add (must not be null or empty)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the patterns array is null, empty, or contains null elements
         */
        @Nonnull
        public Builder<E> patterns(@Nonnull String... patterns) throws IllegalArgumentException {
            return patterns(ofList(patterns));
        }

        /**
         * Set multiple path patterns to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.patterns(Arrays.asList("/api/users", "/api/orders"));
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.patterns(Arrays.asList("/api/products/{id}", "/api/categories/{categoryId}"));
         * }</pre>
         *
         * @param patterns the collection of path patterns to add (must not be null or empty)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the patterns collection is null, empty, or contains null elements
         */
        @Nonnull
        public Builder<E> patterns(@Nonnull Collection<String> patterns) {
            assertNotEmpty(patterns, () -> "The 'patterns' must not be empty");
            assertNoNullElements(patterns, () -> "The 'patterns' must not contain null element");
            this.patterns = newLinkedHashSet(patterns);
            return this;
        }

        /**
         * Add a single HTTP method to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.method(HttpMethod.GET);
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.method(HttpMethod.POST);
         * }</pre>
         *
         * @param method the HTTP method to add (must not be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the method is null
         */
        @Nonnull
        public Builder<E> method(@Nonnull HttpMethod method) throws IllegalArgumentException {
            assertNotNull(method, () -> "The 'method' must not be null");
            return method(method.name());
        }

        /**
         * Add a single HTTP method to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.method("GET");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.method("POST");
         * }</pre>
         *
         * @param method the HTTP method to add (must not be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the method is null
         */
        @Nonnull
        public Builder<E> method(@Nonnull String method) throws IllegalArgumentException {
            assertNotNull(method, () -> "The 'method' must not be null");
            if (this.methods == null) {
                this.methods = newSet();
            }
            this.methods.add(method);
            return this;
        }

        /**
         * Set multiple HTTP methods to the endpoint mapping from a collection of values.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint with a list of HttpMethod enums
         * List<HttpMethod> methodList = Arrays.asList(HttpMethod.GET, HttpMethod.POST);
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.methods(methodList, HttpMethod::name);
         *
         * // For a WebFlux endpoint with custom objects that have a getMethodName() method
         * List<MyHttpMethod> methods = getCustomMethodObjects();
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.methods(methods, MyHttpMethod::getMethodName);
         * }</pre>
         *
         * @param values         the collection of values to convert to HTTP methods (must not be null)
         * @param stringFunction the function to convert each value to an HTTP method string (must not be null)
         * @param <V>            the type of values in the collection
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the values or stringFunction is null
         */
        @Nonnull
        public <V> Builder<E> methods(Collection<V> values, Function<V, String> stringFunction) {
            return methods(toStrings(values, stringFunction));
        }

        /**
         * Set multiple HTTP methods to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.methods(HttpMethod.GET, HttpMethod.POST);
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.methods(HttpMethod.PUT, HttpMethod.DELETE);
         * }</pre>
         *
         * @param methods the HTTP methods to add (must not be null or empty)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the methods array is null, empty, or contains null elements
         */
        @Nonnull
        public Builder<E> methods(@Nonnull HttpMethod... methods) throws IllegalArgumentException {
            assertNotEmpty(methods, () -> "The 'methods' must not be empty");
            assertNoNullElements(methods, () -> "The 'methods' must not contain null element");
            return methods(toStrings(methods, HttpMethod::name));
        }

        /**
         * Set multiple HTTP methods to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.methods("GET", "POST");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.methods("PUT", "DELETE");
         * }</pre>
         *
         * @param methods the HTTP methods to add (must not be null or empty)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the methods array is null, empty, or contains null elements
         */
        @Nonnull
        public Builder<E> methods(@Nonnull String... methods) throws IllegalArgumentException {
            return methods(ofList(methods));
        }

        public Builder<E> methods(@Nonnull Collection<String> methods) throws IllegalArgumentException {
            assertNotEmpty(methods, () -> "The 'methods' must not be empty");
            assertNoNullElements(methods, () -> "The 'methods' must not contain null element");
            this.methods = newLinkedHashSet(methods);
            return this;
        }

        /**
         * Add a single request parameter to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.param("version", "v1");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.param("userId", 12345);
         * }</pre>
         *
         * @param name  the parameter name (must not be blank)
         * @param value the parameter value (can be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the name is blank
         */
        @Nonnull
        public Builder<E> param(@Nonnull String name, @Nullable String value) throws IllegalArgumentException {
            String nameAndValue = pair(name, value);
            return param(nameAndValue);
        }

        /**
         * Add a single request parameter to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.param("version=v1");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.param("userId=12345");
         * }</pre>
         *
         * @param nameAndValue the parameter name and value in the format "name=value" (must not be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the nameAndValue is null
         */
        @Nonnull
        public Builder<E> param(@Nonnull String nameAndValue) throws IllegalArgumentException {
            if (this.params == null) {
                this.params = newSet();
            }
            this.params.add(nameAndValue);
            return this;
        }

        /**
         * Set multiple request parameters to the endpoint mapping from a collection of values.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint with a list of parameter objects
         * List<MyParam> paramList = Arrays.asList(new MyParam("version", "v1"), new MyParam("lang", "en"));
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.params(paramList, p -> p.getName() + "=" + p.getValue());
         *
         * // For a WebFlux endpoint with custom objects that have a toString() method
         * List<MyQueryParam> queryParams = getCustomParamObjects();
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.params(queryParams, MyQueryParam::toString);
         * }</pre>
         *
         * @param values         the collection of values to convert to parameters (must not be null)
         * @param stringFunction the function to convert each value to a parameter string (must not be null)
         * @param <V>            the type of values in the collection
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the values or stringFunction is null
         */
        @Nonnull
        public <V> Builder<E> params(Collection<V> values, Function<V, String> stringFunction) {
            return params(toStrings(values, stringFunction));
        }

        /**
         * Set multiple request parameters to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.params("version=v1", "lang=en");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.params("userId=12345", "active=true");
         * }</pre>
         *
         * @param params the request parameters to add (can be null or empty)
         * @return this builder instance for method chaining
         */
        @Nonnull
        public Builder<E> params(@Nullable String... params) {
            if (isNotEmpty(params)) {
                this.params = newLinkedHashSet(params);
            }
            return this;
        }

        /**
         * Add a single request header to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.header("X-API-Version", "v1");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.header("Authorization", "Bearer token");
         * }</pre>
         *
         * @param name  the header name (must not be blank)
         * @param value the header value (can be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the name is blank
         */
        @Nonnull
        public <V> Builder<E> header(@Nonnull String name, @Nullable String value) {
            if (CONTENT_TYPE.equals(name)) {
                return consume(value);
            } else if (ACCEPT.equals(name)) {
                return produce(value);
            }
            String nameAndValue = pair(name, value);
            return header(nameAndValue);
        }

        /**
         * Add a single request header to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.header("X-API-Version:v1");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.header("Authorization:Bearer token");
         * }</pre>
         *
         * @param nameAndValue the header name and value in the format "name:value" (must not be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the nameAndValue is null
         */
        @Nonnull
        public Builder<E> header(@Nonnull String nameAndValue) throws IllegalArgumentException {
            assertNotNull(nameAndValue, () -> "The 'nameAndValue' must not be null");
            if (this.headers == null) {
                this.headers = newSet();
            }
            this.headers.add(nameAndValue);
            return this;
        }

        /**
         * Set multiple request headers to the endpoint mapping from a collection of values.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint with a list of header objects
         * List<MyHeader> headerList = Arrays.asList(new MyHeader("X-API-Version", "v1"), new MyHeader("Accept-Language", "en"));
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.headers(headerList, h -> h.getName() + ":" + h.getValue());
         *
         * // For a WebFlux endpoint with custom objects that have a toString() method
         * List<MyRequestHeader> requestHeaders = getCustomHeaderObjects();
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.headers(requestHeaders, MyRequestHeader::toString);
         * }</pre>
         *
         * @param values         the collection of values to convert to headers (must not be null)
         * @param stringFunction the function to convert each value to a header string (must not be null)
         * @param <V>            the type of values in the collection
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the values or stringFunction is null
         */
        @Nonnull
        public <V> Builder<E> headers(Collection<V> values, Function<V, String> stringFunction) {
            return headers(toStrings(values, stringFunction));
        }

        /**
         * Set multiple request headers to the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.headers("X-API-Version:v1", "Accept-Language:en");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.headers("Authorization:Bearer token", "X-Request-ID:12345");
         * }</pre>
         *
         * @param headers the request headers to add (can be null or empty)
         * @return this builder instance for method chaining
         */
        @Nonnull
        public Builder<E> headers(String... headers) {
            if (isNotEmpty(headers)) {
                this.headers = newLinkedHashSet(headers);
            }
            return this;
        }

        /**
         * Add a single media type to the endpoint mapping that it can consume.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.consume(MediaType.APPLICATION_JSON);
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.consume(MediaType.TEXT_PLAIN);
         * }</pre>
         *
         * @param mediaType the media type to consume (must not be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the mediaType is null
         */
        @Nonnull
        public Builder<E> consume(MediaType mediaType) {
            assertNotNull(mediaType, () -> "The 'mediaType' must not be null");
            return consume(mediaType.toString());
        }

        /**
         * Add a single media type to the endpoint mapping that it can consume.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.consume("application/json");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.consume("text/plain");
         * }</pre>
         *
         * @param consume the media type to consume (must not be blank)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the consume is blank
         */
        @Nonnull
        public Builder<E> consume(String consume) {
            assertNotBlank(consume, () -> "The 'consume' must not be blank");
            if (this.consumes == null) {
                this.consumes = newSet();
            }
            this.consumes.add(consume);
            return this;
        }

        /**
         * Set multiple media types to the endpoint mapping that it can consume.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.consumes(MediaType.APPLICATION_JSON, MediaType.TEXT_XML);
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.consumes(MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM);
         * }</pre>
         *
         * @param mediaTypes the media types to consume (must not be null or contain null elements)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the mediaTypes array is null or contains null elements
         */
        @Nonnull
        public Builder<E> consumes(MediaType... mediaTypes) {
            return consumes(toStrings(mediaTypes, MediaType::toString));
        }

        /**
         * Set multiple media types to the endpoint mapping that it can consume from a collection of values.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint with a list of MediaType objects
         * List<MediaType> mediaTypeList = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_XML);
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.consumes(mediaTypeList, MediaType::toString);
         *
         * // For a WebFlux endpoint with custom objects that have a getMimeType() method
         * List<MyMediaType> customMediaTypes = getCustomMediaTypes();
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.consumes(customMediaTypes, MyMediaType::getMimeType);
         * }</pre>
         *
         * @param values         the collection of values to convert to media types (must not be null)
         * @param stringFunction the function to convert each value to a media type string (must not be null)
         * @param <V>            the type of values in the collection
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the values or stringFunction is null
         */
        @Nonnull
        public <V> Builder<E> consumes(Collection<V> values, Function<V, String> stringFunction) {
            return consumes(toStrings(values, stringFunction));
        }

        /**
         * Set multiple media types to the endpoint mapping that it can consume.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.consumes("application/json", "text/xml");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.consumes("text/plain", "application/octet-stream");
         * }</pre>
         *
         * @param consumes the media types to consume (can be null or empty)
         * @return this builder instance for method chaining
         */
        @Nonnull
        public Builder<E> consumes(String... consumes) {
            if (isNotEmpty(consumes)) {
                this.consumes = newLinkedHashSet(consumes);
            }
            return this;
        }

        /**
         * Add a single media type to the endpoint mapping that it can produce.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.produce(MediaType.APPLICATION_JSON);
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.produce(MediaType.TEXT_PLAIN);
         * }</pre>
         *
         * @param mediaType the media type to produce (must not be null)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the mediaType is null
         */
        @Nonnull
        public Builder<E> produce(MediaType mediaType) {
            assertNotNull(mediaType, () -> "The 'mediaType' must not be null");
            return produce(mediaType.toString());
        }

        /**
         * Add a single media type to the endpoint mapping that it can produce.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.produce("application/json");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.produce("text/plain");
         * }</pre>
         *
         * @param produce the media type to produce (must not be blank)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the produce is blank
         */
        @Nonnull
        public Builder<E> produce(String produce) {
            assertNotBlank(produce, () -> "The 'produce' must not be blank");
            if (this.produces == null) {
                this.produces = newSet();
            }
            this.produces.add(produce);
            return this;
        }

        /**
         * Set multiple media types to the endpoint mapping that it can produce.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.produces(MediaType.APPLICATION_JSON, MediaType.TEXT_XML);
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.produces(MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM);
         * }</pre>
         *
         * @param mediaTypes the media types to produce (must not be null or contain null elements)
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the mediaTypes array is null or contains null elements
         */
        @Nonnull
        public Builder<E> produces(MediaType... mediaTypes) {
            assertNotNull(mediaTypes, () -> "The 'mediaTypes' must not be null");
            assertNoNullElements(mediaTypes, () -> "The 'mediaTypes' must not be null");
            return produces(toStrings(mediaTypes, MimeType::toString));
        }

        /**
         * Set multiple media types to the endpoint mapping that it can produce from a collection of values.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint with a list of MediaType objects
         * List<MediaType> mediaTypeList = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_XML);
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.produces(mediaTypeList, MediaType::toString);
         *
         * // For a WebFlux endpoint with custom objects that have a getMimeType() method
         * List<MyMediaType> customMediaTypes = getCustomMediaTypes();
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.produces(customMediaTypes, MyMediaType::getMimeType);
         * }</pre>
         *
         * @param values         the collection of values to convert to media types (must not be null)
         * @param stringFunction the function to convert each value to a media type string (must not be null)
         * @param <V>            the type of values in the collection
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if the values or stringFunction is null
         */
        @Nonnull
        public <V> Builder<E> produces(Collection<V> values, Function<V, String> stringFunction) {
            return produces(toStrings(values, stringFunction));
        }

        /**
         * Set multiple media types to the endpoint mapping that it can produce.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.produces("application/json", "text/xml");
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.produces("text/plain", "application/octet-stream");
         * }</pre>
         *
         * @param produces the media types to produce (can be null or empty)
         * @return this builder instance for method chaining
         */
        @Nonnull
        public Builder<E> produces(String... produces) {
            if (isNotEmpty(produces)) {
                this.produces = newLinkedHashSet(produces);
            }
            return this;
        }

        /**
         * Set the source of the endpoint mapping.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         * // For a servlet endpoint
         * WebEndpointMapping.Builder<String> builder = WebEndpointMapping.servlet("myServlet");
         * builder.source(servletContext);
         *
         * // For a WebFlux endpoint
         * WebEndpointMapping.Builder<HandlerMethod> webFluxBuilder = WebEndpointMapping.webflux(handlerMethod);
         * webFluxBuilder.source(handlerMapping);
         * }</pre>
         *
         * @param source the source object (can be null)
         * @return this builder instance for method chaining
         */
        @Nonnull
        public Builder<E> source(Object source) {
            this.source = source;
            return this;
        }

        @Nonnull
        public Builder<E> nestPatterns(@Nonnull Builder<?> other) {
            assertNest(this, other);
            Set<String> patterns = newSet();
            iterate(this.patterns, pattern -> {
                iterate(other.patterns, otherPattern -> {
                    patterns.add(buildURI(otherPattern, pattern));
                });
            });
            this.patterns = patterns;
            return this;
        }

        @Nonnull
        public Builder<E> nestMethods(@Nonnull Builder<?> other) {
            assertNest(this, other);
            iterate(other.methods, this::method);
            return this;
        }

        @Nonnull
        public Builder<E> nestParams(@Nonnull Builder<?> other) {
            assertNest(this, other);
            iterate(other.params, this::param);
            return this;
        }

        @Nonnull
        public Builder<E> nestHeaders(@Nonnull Builder<?> other) {
            assertNest(this, other);
            iterate(other.headers, this::header);
            return this;
        }

        @Nonnull
        public Builder<E> nestConsumes(@Nonnull Builder<?> other) {
            assertNest(this, other);
            iterate(other.consumes, this::consume);
            return this;
        }

        @Nonnull
        public Builder<E> nestProduces(@Nonnull Builder<?> other) {
            assertNest(this, other);
            iterate(other.produces, this::produce);
            return this;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("WebEndpointMapping.Builder{");
            sb.append("kind=").append(kind);
            sb.append(", endpoint=").append(endpoint);
            sb.append(", source=").append(source);
            sb.append(", patterns=").append(patterns);
            sb.append(", methods=").append(methods);
            sb.append(", params=").append(params);
            sb.append(", headers=").append(headers);
            sb.append(", consumes=").append(consumes);
            sb.append(", produces=").append(produces);
            sb.append('}');
            return sb.toString();
        }

        protected static String pair(String name, @Nullable Object value) {
            assertNotBlank(name, () -> "The 'name' must not be blank");
            return value == null ? name : name + EQUAL_CHAR + value;
        }

        static void assertNest(Builder<?> one, Builder<?> other) {
            assertNotNull(one, () -> "The 'one' Builder must not be null!");
            assertNotNull(other, () -> "The 'other' Builder must not be null!");
            assertTrue(one.kind == other.kind, () -> format("The Kind does not match[one : {} , other : {}]", one.kind, other.kind));
        }

        protected static Set<String> newSet() {
            return newLinkedHashSet(2);
        }

        protected static String[] toStrings(Collection<String> values) {
            return toStrings(values, identity());
        }

        static <V> String[] toStrings(V[] values, Function<V, String> stringFunction) {
            return toStrings(ofList(values), stringFunction);
        }

        protected static <V> String[] toStrings(Collection<V> values, Function<V, String> stringFunction) {
            if (isEmpty(values)) {
                return EMPTY_STRING_ARRAY;
            }
            return values.stream().map(stringFunction).toArray(String[]::new);
        }

        /**
         * Build {@link WebEndpointMapping}
         *
         * @return non-null
         * @throws IllegalArgumentException if "endpoint" or "patterns" or "methods" is empty or has any null element
         */
        @Nonnull
        public WebEndpointMapping build() throws IllegalArgumentException {
            assertNotNull(this.endpoint, () -> "The 'endpoint' must not be null");

            assertNotEmpty(this.patterns, () -> "The 'pattern' must not be empty");
            assertNoNullElements(this.patterns, "Any element of 'patterns' must not be null");

            assertNotEmpty(this.methods, () -> "The 'methods' must not be empty");
            assertNoNullElements(this.methods, "Any element of 'methods' must not be null");

            return new WebEndpointMapping(
                    this.kind,
                    this.endpoint,
                    this.source,
                    toStrings(this.patterns),
                    toStrings(this.methods),
                    toStrings(this.params),
                    toStrings(this.headers),
                    toStrings(this.consumes),
                    toStrings(this.produces)
            );
        }
    }

    /**
     * Create a {@link Builder} of {@link WebEndpointMapping} for {@link Kind#SERVLET servlet}.
     *
     * @param <E> the type of endpoint
     * @return a {@link Builder} of {@link WebEndpointMapping}
     * @throws IllegalArgumentException if the {@code endpoint} is {@code null}
     */
    @Nonnull
    public static <E> Builder<E> servlet() throws IllegalArgumentException {
        return of(SERVLET);
    }

    /**
     * Create a {@link Builder} of {@link WebEndpointMapping} for {@link Kind#FILTER filter}.
     *
     * @param <E> the type of endpoint
     * @return a {@link Builder} of {@link WebEndpointMapping}
     * @throws IllegalArgumentException if the {@code endpoint} is {@code null}
     */
    @Nonnull
    public static <E> Builder<E> filter() throws IllegalArgumentException {
        return of(FILTER);
    }

    /**
     * Create a {@link Builder} of {@link WebEndpointMapping} for {@link Kind#WEB_MVC webMvc}.
     *
     * @param <E> the type of endpoint
     * @return a {@link Builder} of {@link WebEndpointMapping}
     * @throws IllegalArgumentException if the {@code endpoint} is {@code null}
     */
    @Nonnull
    public static <E> Builder<E> webmvc() throws IllegalArgumentException {
        return of(WEB_MVC);
    }

    /**
     * Create a {@link Builder} of {@link WebEndpointMapping} for {@link Kind#WEB_FLUX webFlux}.
     *
     * @param <E> the type of endpoint
     * @return a {@link Builder} of {@link WebEndpointMapping}
     * @throws IllegalArgumentException if the {@code endpoint} is {@code null}
     */
    @Nonnull
    public static <E> Builder<E> webflux() throws IllegalArgumentException {
        return of(WEB_FLUX);
    }

    /**
     * Create a {@link Builder} of {@link WebEndpointMapping} for {@link Kind#CUSTOMIZED customized}.
     *
     * @param <E> the type of endpoint
     * @return a {@link Builder} of {@link WebEndpointMapping}
     * @throws IllegalArgumentException if the {@code endpoint} is {@code null}
     */
    @Nonnull
    public static <E> Builder<E> customized() throws IllegalArgumentException {
        return of(CUSTOMIZED);
    }

    /**
     * Create a {@link Builder} of {@link WebEndpointMapping} with specified kind.
     *
     * @param kind the kind of endpoint
     * @param <E>  the type of endpoint
     * @return a {@link Builder} of {@link WebEndpointMapping}
     * @throws IllegalArgumentException if the {@code kind} or {@code endpoint} is {@code null}
     */
    @Nonnull
    public static <E> Builder<E> of(@Nonnull Kind kind) throws IllegalArgumentException {
        return new Builder<>(kind);
    }

    private WebEndpointMapping(
            @Nonnull Kind kind,
            @Nonnull E endpoint,
            @Nullable Object source,
            @Nonnull String[] patterns,
            @Nullable String[] methods,
            @Nullable String[] params,
            @Nullable String[] headers,
            @Nullable String[] consumes,
            @Nullable String[] produces) {
        this.kind = kind;
        this.endpoint = endpoint;
        // id is a hash code of the endpoint
        this.id = endpoint == null ? 0 : endpoint.hashCode();
        this.source = source == null ? UNKNOWN_SOURCE : source;
        this.patterns = patterns;
        this.methods = methods;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    /**
     * For serialization
     */
    private WebEndpointMapping() {
        this(null, null, null, null, null, null, null, null, null);
    }

    /**
     * The kind of endpoint:
     * <ul>
     *     <li>{@link javax.servlet.Servlet Servlet}</li>
     *     <li>{@link javax.servlet.Filter Servlet's Filter}</li>
     *     <li>Spring WebMVC {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}</li>
     *     <li>Spring WebFlux {@link org.springframework.web.reactive.DispatcherHandler DispatcherHandler}</li>
     *     <li>Customized</li>
     * </ul>
     *
     * @return non-null
     */
    @Nonnull
    public Kind getKind() {
        return kind;
    }

    /**
     * The abstract presentation of actual endpoint that may be :
     * <ul>
     *     <li>{@link javax.servlet.ServletRegistration#getName() the name of Servlet}</li>
     *     <li>{@link javax.servlet.FilterRegistration#getName() the name of Servlet's Filter}</li>
     *     <li>the any handler of Spring WebMVC {@link org.springframework.web.servlet.HandlerMapping}:
     *      <ul>
     *          <li>The {@link String} presenting the name of Handler bean</li>
     *          <li>The {@link org.springframework.web.servlet.mvc.Controller} Bean</li>
     *          <li>The {@link HandlerMethod} could be annotated the {@link RequestMapping @RequestMapping}</li>
     *          <li>The {@link org.springframework.web.servlet.function.HandlerFunction} since Spring Framework 5.2</li>
     *      </ul>
     *     </li>
     *     <li>the any handler of Spring WebFlux {@link org.springframework.web.reactive.DispatcherHandler}:
     *      <ul>
     *          <li>The {@link String} presenting the name of Handler bean</li>
     *          <li>The {@link HandlerMethod} could be annotated the {@link RequestMapping @RequestMapping}</li>
     *          <li>The {@link org.springframework.web.reactive.function.server.HandlerFunction} since Spring Framework 5.0</li>
     *      </ul>
     *     </li>
     * </ul>
     *
     * @return non-null
     */
    @Nonnull
    public E getEndpoint() {
        return endpoint;
    }

    /**
     * The id of endpoint
     *
     * @return id of endpoint
     */
    public int getId() {
        return id;
    }

    /**
     * The source of {@link WebEndpointMapping} if present, it could be :
     * <ul>
     *     <li>{@link javax.servlet.ServletContext ServletContext}</li>
     *     <li>Spring WebMVC {@link org.springframework.web.servlet.HandlerMapping}</li>
     *     <li>Spring WebFlux {@link org.springframework.web.reactive.HandlerMapping}</li>
     * </ul>, or it's {@link #UNKNOWN_SOURCE non-source}
     *
     * @return non-null
     */
    @Nonnull
    public Object getSource() {
        return this.source;
    }

    @Nonnull
    public String[] getPatterns() {
        return patterns;
    }

    @Nonnull
    public String[] getMethods() {
        return methods;
    }

    @Nonnull
    public String[] getParams() {
        return params;
    }

    @Nonnull
    public String[] getHeaders() {
        return headers;
    }

    @Nonnull
    public String[] getConsumes() {
        return consumes;
    }

    @Nonnull
    public String[] getProduces() {
        return produces;
    }

    public <V> WebEndpointMapping<E> setAttribute(String name, @Nullable V value) {
        if (value != null) {
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put(name, value);
        }
        return this;
    }

    @Nullable
    public <V> V getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return (V) attributes.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebEndpointMapping that = (WebEndpointMapping) o;
        return arrayEquals(patterns, that.patterns)
                && arrayEquals(methods, that.methods)
                && arrayEquals(params, that.params)
                && arrayEquals(headers, that.headers)
                && arrayEquals(consumes, that.consumes)
                && arrayEquals(produces, that.produces);
    }

    @Override
    public int hashCode() {
        int hashCode = this.hashCode;
        if (hashCode == 0) {
            hashCode = Arrays.hashCode(patterns);
            if (methods != null) {
                hashCode = 31 * hashCode + Arrays.hashCode(methods);
            }
            if (params != null) {
                hashCode = 31 * hashCode + Arrays.hashCode(params);
            }
            if (headers != null) {
                hashCode = 31 * hashCode + Arrays.hashCode(headers);
            }
            if (consumes != null) {
                hashCode = 31 * hashCode + Arrays.hashCode(consumes);
            }
            if (produces != null) {
                hashCode = 31 * hashCode + Arrays.hashCode(produces);
            }
            this.hashCode = hashCode;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WebEndpointMapping{");
        sb.append("kind=").append(kind);
        sb.append(", endpoint=").append(endpoint);
        sb.append(", id=").append(id);
        sb.append(", source=").append(source);
        sb.append(", patterns=").append(arrayToString(patterns));
        sb.append(", methods=").append(arrayToString(methods));
        sb.append(", params=").append(arrayToString(params));
        sb.append(", headers=").append(arrayToString(headers));
        sb.append(", consumes=").append(arrayToString(consumes));
        sb.append(", produces=").append(arrayToString(produces));
        sb.append('}');
        return sb.toString();
    }

    public String toJSON() {
        StringBuilder stringBuilder = new StringBuilder(LEFT_CURLY_BRACE).append(LINE_SEPARATOR);
        append(stringBuilder, "id", this.id);
        append(stringBuilder, "patterns", this.patterns, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "methods", this.methods, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "params", this.params, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "headers", this.headers, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "consumes", this.consumes, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "produces", this.produces, COMMA, LINE_SEPARATOR);
        stringBuilder.append(LINE_SEPARATOR).append(RIGHT_CURLY_BRACE);
        return stringBuilder.toString();
    }

    private void append(StringBuilder appendable, String name, int value) {
        JSONUtils.append(appendable, name, value);
    }

    private void append(StringBuilder appendable, String name, String[] values, String... prefixes) {
        if (isEmpty(values)) {
            return;
        }
        append(prefixes, appendable);
        JSONUtils.append(appendable, name, values);
    }

    private void append(String[] values, StringBuilder appendable) {
        for (int i = 0; i < values.length; i++) {
            appendable.append(values[i]);
        }
    }
}
