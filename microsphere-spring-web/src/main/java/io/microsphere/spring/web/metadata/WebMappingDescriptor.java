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
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTATION_CHAR;

/**
 * The descriptor class for Web Mapping that could be one of these sources:
 * <ul>
 *     <li>Servlet Mapping</li>
 *     <li>Servlet's Filter Mapping</li>
 *     <li>Spring WebMVC Mapping</li>
 *     <li>Spring WebFlux Mapping</li>
 * </ul>
 *
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
public class WebMappingDescriptor {

    private final transient Object source;

    private final String[] patterns;

    private final String[] methods;

    private final String[] params;

    private final String[] headers;

    private final String[] consumes;

    private final String[] produces;

    public static class Builder {

        private final Object source;

        private String[] patterns;

        private String[] methods;

        private String[] params;

        private String[] headers;

        private String[] consumes;

        private String[] produces;

        private Builder(Object source) {
            Assert.notNull(source, "The source must not be null!");
            this.source = source;
        }

        public Builder patterns(String... patterns) {
            this.patterns = patterns;
            return this;
        }

        public Builder methods(String... methods) {
            this.methods = methods;
            return this;
        }

        public Builder params(String... params) {
            this.params = params;
            return this;
        }

        public Builder headers(String... headers) {
            this.headers = headers;
            return this;
        }

        public Builder consumes(String... consumes) {
            this.consumes = consumes;
            return this;
        }

        public Builder produces(String... produces) {
            this.produces = produces;
            return this;
        }

        public WebMappingDescriptor build() {
            return new WebMappingDescriptor(this.source,
                    this.patterns,
                    this.methods,
                    this.params,
                    this.headers,
                    this.consumes,
                    this.produces
            );
        }

    }

    public static Builder source(Object source) {
        return new Builder(source);
    }

    private WebMappingDescriptor(Object source,
                                 String[] patterns,
                                 @Nullable String[] methods,
                                 @Nullable String[] params,
                                 @Nullable String[] headers,
                                 @Nullable String[] consumes,
                                 @Nullable String[] produces) {
        this.source = source;
        this.patterns = patterns;
        this.methods = methods;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    public HandlerMethod getHandlerMethod() {
        Object source = getSource();
        if (source instanceof HandlerMethod) {
            return (HandlerMethod) source;
        }
        return null;
    }

    public Object getSource() {
        return source;
    }

    public String[] getPatterns() {
        return patterns;
    }

    public String[] getMethods() {
        return methods;
    }

    public String[] getParams() {
        return params;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String[] getConsumes() {
        return consumes;
    }

    public String[] getProduces() {
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
