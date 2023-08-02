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

import io.microsphere.util.ArrayUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTATION_CHAR;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.CUSTOMIZED;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static org.springframework.util.Assert.isTrue;
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
 *          <li>The {@link org.springframework.web.reactive.function.server.RouterFunction} since Spring Framework 5.0</li>
 *      </ul>
 *     </li>
 * </ul>
 * <p>
 * The method {@link #getSource()} can trace the source of {@link WebEndpointMapping} if present, it could be :
 * <ul>
 *     <li>{@link javax.servlet.ServletContext ServletContext}</li>
 *     <li>Spring WebMVC {@link org.springframework.web.servlet.HandlerMapping}</li>
 *     <li>Spring WebFlux {@link org.springframework.web.reactive.HandlerMapping}</li>
 * </ul>, or it's {@link #NON_SOURCE non-source}
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

    public static final Object NON_ENDPOINT = new Object();

    public static final Object NON_SOURCE = new Object();

    private final transient Kind kind;

    private final transient E endpoint;

    private final int id;

    private final String[] patterns;

    private final String[] methods;

    private final String[] params;

    private final String[] headers;

    private final String[] consumes;

    private final String[] produces;

    private transient final Object source;

    private transient int hashCode = 0;

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

        private final Kind kind;

        private final E endpoint;

        private Object source;

        private final String[] patterns;

        private String[] methods;

        private String[] params;

        private String[] headers;

        private String[] consumes;

        private String[] produces;

        private Builder(Kind kind, E endpoint, String[] patterns) {
            isTrue(!isEmpty(patterns), "The patterns must not be empty!");
            this.kind = kind == null ? CUSTOMIZED : kind;
            this.endpoint = endpoint == null ? (E) NON_ENDPOINT : endpoint;
            this.patterns = patterns;
        }

        public <V> Builder<E> methods(Collection<V> values, Function<V, String> stringFunction) {
            return methods(toStrings(values, stringFunction));
        }

        public Builder<E> methods(String... methods) {
            this.methods = methods;
            return this;
        }

        public <V> Builder<E> params(Collection<V> values, Function<V, String> stringFunction) {
            return params(toStrings(values, stringFunction));
        }

        public Builder<E> params(String... params) {
            this.params = params;
            return this;
        }

        public <V> Builder<E> headers(Collection<V> values, Function<V, String> stringFunction) {
            return headers(toStrings(values, stringFunction));
        }

        public Builder<E> headers(String... headers) {
            this.headers = headers;
            return this;
        }

        public <V> Builder<E> consumes(Collection<V> values, Function<V, String> stringFunction) {
            return consumes(toStrings(values, stringFunction));
        }

        public Builder<E> consumes(String... consumes) {
            this.consumes = consumes;
            return this;
        }

        public <V> Builder<E> produces(Collection<V> values, Function<V, String> stringFunction) {
            return produces(toStrings(values, stringFunction));
        }

        public Builder<E> produces(String... produces) {
            this.produces = produces;
            return this;
        }

        public Builder<E> source(Object source) {
            this.source = source;
            return this;
        }

        protected <V> String[] toStrings(Collection<V> values, Function<V, String> stringFunction) {
            if (values.isEmpty()) {
                return EMPTY_STRING_ARRAY;
            }
            return values.stream().map(stringFunction).toArray(String[]::new);
        }

        public WebEndpointMapping build() {
            return new WebEndpointMapping(
                    this.kind,
                    this.endpoint,
                    this.source,
                    this.patterns,
                    this.methods,
                    this.params,
                    this.headers,
                    this.consumes,
                    this.produces
            );
        }

    }

    public static Builder<?> of(Collection<String> patterns) {
        return of(null, null, patterns);
    }

    public static Builder<?> of(String... patterns) {
        return of(null, null, patterns);
    }

    public static <E> Builder<E> of(@Nullable E endpoint, Collection<String> patterns) {
        return of(null, endpoint, patterns);
    }

    public static <E> Builder<E> of(@Nullable E endpoint, String... patterns) {
        return of(null, endpoint, patterns);
    }

    public static <E> Builder<E> of(@Nullable Kind kind, @Nullable E endpoint, Collection<String> patterns) {
        return of(kind, endpoint, ArrayUtils.asArray(patterns, String.class));
    }

    public static <E> Builder of(@Nullable Kind kind, @Nullable E endpoint, String... patterns) {
        return new Builder(kind, endpoint, patterns);
    }

    private WebEndpointMapping(
            Kind kind,
            E endpoint,
            Object source,
            String[] patterns,
            @Nullable String[] methods,
            @Nullable String[] params,
            @Nullable String[] headers,
            @Nullable String[] consumes,
            @Nullable String[] produces) {
        this.kind = kind;
        this.endpoint = endpoint;
        // id is a hash code of the endpoint
        this.id = endpoint == null ? 0 : endpoint.hashCode();
        this.source = source == null ? NON_SOURCE : source;
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
    @NonNull
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
     *          <li>The {@link org.springframework.web.reactive.function.server.RouterFunction} since Spring Framework 5.0</li>
     *      </ul>
     *     </li>
     * </ul>
     *
     * @return non-null
     */
    @NonNull
    public E getEndpoint() {
        return endpoint;
    }

    /**
     * The id of endpoint
     *
     * @return 0 if {@link #NON_ENDPOINT no endpoint present}
     */
    public int getId() {
        return id;
    }

    /**
     * @return
     */
    @NonNull
    public Object getSource() {
        return this.source;
    }

    @NonNull
    public String[] getPatterns() {
        return patterns;
    }

    @NonNull

    public String[] getMethods() {
        if (methods == null) {
            return EMPTY_STRING_ARRAY;
        }
        return methods;
    }

    @NonNull

    public String[] getParams() {
        if (params == null) {
            return EMPTY_STRING_ARRAY;
        }
        return params;
    }

    @NonNull
    public String[] getHeaders() {
        if (headers == null) {
            return EMPTY_STRING_ARRAY;
        }
        return headers;
    }

    @NonNull
    public String[] getConsumes() {
        if (consumes == null) {
            return EMPTY_STRING_ARRAY;
        }
        return consumes;
    }

    @NonNull
    public String[] getProduces() {
        if (produces == null) {
            return EMPTY_STRING_ARRAY;
        }
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

    public String toJSON() {
        StringBuilder stringBuilder = new StringBuilder("{").append(LINE_SEPARATOR);
        append(stringBuilder, "id", this.id);
        append(stringBuilder, "patterns", this.patterns, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "methods", this.methods, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "params", this.params, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "headers", this.headers, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "consumes", this.consumes, COMMA, LINE_SEPARATOR);
        append(stringBuilder, "produces", this.produces, COMMA, LINE_SEPARATOR);
        stringBuilder.append(LINE_SEPARATOR).append("}");
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebEndpointMapping that = (WebEndpointMapping) o;
        return Arrays.equals(patterns, that.patterns)
                && Arrays.equals(methods, that.methods)
                && Arrays.equals(params, that.params)
                && Arrays.equals(headers, that.headers)
                && Arrays.equals(consumes, that.consumes)
                && Arrays.equals(produces, that.produces);
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
        final StringBuilder sb = new StringBuilder("WebMappingDescriptor{");
        sb.append("kind=").append(kind);
        sb.append(", endpoint=").append(endpoint);
        sb.append(", id=").append(id);
        sb.append(", source=").append(source);
        sb.append(", patterns=").append(Arrays.toString(patterns));
        sb.append(", methods=").append(Arrays.toString(methods));
        sb.append(", params=").append(Arrays.toString(params));
        sb.append(", headers=").append(Arrays.toString(headers));
        sb.append(", consumes=").append(Arrays.toString(consumes));
        sb.append(", produces=").append(Arrays.toString(produces));
        sb.append('}');
        return sb.toString();
    }

    private void append(StringBuilder appendable, String name, int value) {
        append(appendable, name);
        appendable.append(COLON_CHAR)
                .append(value);
    }

    private void append(StringBuilder appendable, String name, String[] values, String... prefixes) {

        int size = ArrayUtils.size(values);
        if (size < 1) {
            return;
        }

        append(prefixes, appendable);

        append(appendable, name);

        appendable.append(COLON_CHAR);

        appendable.append('[');

        for (int i = 0; i < size; i++) {
            String value = values[i];
            append(appendable, value);
            if (i != size - 1) {
                appendable.append(COMMA_CHAR);
            }
        }

        appendable.append(']');

    }

    private void append(StringBuilder appendable, String appended) {
        if (appended == null) {
            return;
        }
        appendable.append(DOUBLE_QUOTATION_CHAR)
                .append(appended)
                .append(DOUBLE_QUOTATION_CHAR);
    }

    private void append(String[] values, StringBuilder appendable) {
        for (int i = 0; i < values.length; i++) {
            appendable.append(values[i]);
        }
    }
}
