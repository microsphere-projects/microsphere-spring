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
package io.microsphere.spring.web.bind.annotation;

import io.microsphere.util.ArrayUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTATION_CHAR;

/**
 * The Raw meta-data class for the Spring's annotation {@link RequestMapping @RequestMapping}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMapping
 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfo
 * @see org.springframework.web.reactive.result.method.RequestMappingInfo
 * @since 1.0.0
 */
public class RawRequestMappingMetadata {

    private final String[] patterns;

    private final String[] methods;

    private final String[] params;

    private final String[] headers;

    private final String[] consumes;

    private final String[] produces;

    public RawRequestMappingMetadata(String... patterns) {
        this(patterns, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String... methods) {
        this(patterns, methods, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String... params) {
        this(patterns, methods, params, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String[] params,
                                     @Nullable String... headers) {
        this(patterns, methods, params, headers, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String[] params,
                                     @Nullable String[] headers,
                                     @Nullable String... consumes) {
        this(patterns, methods, params, headers, consumes, null);
    }

    public RawRequestMappingMetadata(String[] patterns,
                                     @Nullable String[] methods,
                                     @Nullable String[] params,
                                     @Nullable String[] headers,
                                     @Nullable String[] consumes,
                                     @Nullable String... produces) {
        this.patterns = patterns;
        this.methods = methods;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawRequestMappingMetadata that = (RawRequestMappingMetadata) o;
        return Arrays.equals(patterns, that.patterns) && Arrays.equals(methods, that.methods) && Arrays.equals(params, that.params) && Arrays.equals(headers, that.headers) && Arrays.equals(consumes, that.consumes) && Arrays.equals(produces, that.produces);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(patterns);
        result = 31 * result + Arrays.hashCode(methods);
        result = 31 * result + Arrays.hashCode(params);
        result = 31 * result + Arrays.hashCode(headers);
        result = 31 * result + Arrays.hashCode(consumes);
        result = 31 * result + Arrays.hashCode(produces);
        return result;
    }

    @Override
    public String toString() {
        return "RawRequestMappingMetadata{" +
                "patterns=" + Arrays.toString(patterns) +
                ", methods=" + Arrays.toString(methods) +
                ", params=" + Arrays.toString(params) +
                ", headers=" + Arrays.toString(headers) +
                ", consumes=" + Arrays.toString(consumes) +
                ", produces=" + Arrays.toString(produces) +
                '}';
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
