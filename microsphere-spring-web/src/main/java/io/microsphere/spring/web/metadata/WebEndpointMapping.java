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

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTATION_CHAR;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.UNKNOWN;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * The meta-data class for Web Endpoint Mapping that could be one of these sources:
 * <ul>
 *     <li>Servlet Mapping</li>
 *     <li>Servlet's Filter Mapping</li>
 *     <li>Spring WebMVC Mapping</li>
 *     <li>Spring WebFlux Mapping</li>
 * </ul>
 *
 * @param <S> the type of source
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see javax.servlet.ServletRegistration
 * @see javax.servlet.FilterRegistration
 * @see javax.servlet.annotation.WebServlet
 * @see javax.servlet.annotation.WebFilter
 * @see org.springframework.web.servlet.HandlerMapping
 * @see org.springframework.web.reactive.HandlerMapping
 * @see RequestMapping
 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfo
 * @see org.springframework.web.reactive.result.method.RequestMappingInfo
 * @since 1.0.0
 */
public class WebEndpointMapping<S> {

    /**
     * The HTTP header name for {@link WebEndpointMapping#getId()}
     */
    public static final String ID_HEAD_NAME = "web::endpoint::id";

    public static final Object NON_SOURCE = new Object();

    private final transient Kind kind;

    private final transient S source;

    private final int id;

    private final String[] patterns;

    private final String[] methods;

    private final String[] params;

    private final String[] headers;

    private final String[] consumes;

    private final String[] produces;

    private transient int hashCode = 0;


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
         * Spring WebMVC
         */
        WEB_MVC,

        /**
         * Spring WebFlux
         */
        WEB_FLUX,

        /**
         * Unknown
         */
        UNKNOWN,

    }

    public static class Builder<S> {

        private final Kind kind;

        private final Object source;

        private final String[] patterns;

        private String[] methods;

        private String[] params;

        private String[] headers;

        private String[] consumes;

        private String[] produces;

        private Builder(Kind kind, S source, String[] patterns) {
            isTrue(!isEmpty(patterns), "The patterns must not be empty!");
            this.kind = kind == null ? UNKNOWN : kind;
            this.source = source == null ? NON_SOURCE : source;
            this.patterns = patterns;
        }

        public <V> Builder<S> methods(Collection<V> values, Function<V, String> stringFunction) {
            return methods(toStrings(values, stringFunction));
        }

        public Builder<S> methods(String... methods) {
            this.methods = methods;
            return this;
        }

        public <V> Builder<S> params(Collection<V> values, Function<V, String> stringFunction) {
            return params(toStrings(values, stringFunction));
        }

        public Builder<S> params(String... params) {
            this.params = params;
            return this;
        }

        public <V> Builder<S> headers(Collection<V> values, Function<V, String> stringFunction) {
            return headers(toStrings(values, stringFunction));
        }

        public Builder<S> headers(String... headers) {
            this.headers = headers;
            return this;
        }

        public <V> Builder<S> consumes(Collection<V> values, Function<V, String> stringFunction) {
            return consumes(toStrings(values, stringFunction));
        }

        public Builder<S> consumes(String... consumes) {
            this.consumes = consumes;
            return this;
        }

        public <V> Builder<S> produces(Collection<V> values, Function<V, String> stringFunction) {
            return produces(toStrings(values, stringFunction));
        }

        public Builder<S> produces(String... produces) {
            this.produces = produces;
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

    public static <S> Builder<S> of(@Nullable Kind kind, @Nullable S source, Collection<String> patterns) {
        return of(kind, source, ArrayUtils.asArray(patterns, String.class));
    }

    public static <S> Builder of(@Nullable Kind kind, @Nullable S source, String... patterns) {
        return new Builder(kind, source, patterns);
    }

    private WebEndpointMapping(
            Kind kind,
            S source,
            String[] patterns,
            @Nullable String[] methods,
            @Nullable String[] params,
            @Nullable String[] headers,
            @Nullable String[] consumes,
            @Nullable String[] produces) {
        this.kind = kind;
        this.source = source;
        // id is a hash code of the source
        this.id = source == null ? 0 : source.hashCode();
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
        this(UNKNOWN, null, null, null, null, null, null, null);
    }

    public Kind getKind() {
        return kind;
    }

    public S getSource() {
        return source;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String[] getPatterns() {
        return patterns;
    }

    public String[] getMethods() {
        if (methods == null) {
            return EMPTY_STRING_ARRAY;
        }
        return methods;
    }

    public String[] getParams() {
        if (params == null) {
            return EMPTY_STRING_ARRAY;
        }
        return params;
    }

    public String[] getHeaders() {
        if (headers == null) {
            return EMPTY_STRING_ARRAY;
        }
        return headers;
    }

    public String[] getConsumes() {
        if (consumes == null) {
            return EMPTY_STRING_ARRAY;
        }
        return consumes;
    }

    public String[] getProduces() {
        if (produces == null) {
            return EMPTY_STRING_ARRAY;
        }
        return produces;
    }

    public String toJSON() {
        StringBuilder stringBuilder = new StringBuilder("{").append(LINE_SEPARATOR);
        append(stringBuilder, "patterns", this.patterns);
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
        sb.append(", source=").append(source);
        sb.append(", id=").append(id);
        sb.append(", patterns=").append(Arrays.toString(patterns));
        sb.append(", methods=").append(Arrays.toString(methods));
        sb.append(", params=").append(Arrays.toString(params));
        sb.append(", headers=").append(Arrays.toString(headers));
        sb.append(", consumes=").append(Arrays.toString(consumes));
        sb.append(", produces=").append(Arrays.toString(produces));
        sb.append('}');
        return sb.toString();
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
