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

import io.microsphere.spring.web.rule.MediaTypeExpression;
import io.microsphere.spring.web.rule.NameValueExpression;
import io.microsphere.util.ArrayUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.function.Function;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.COMMA_CHAR;
import static io.microsphere.constants.SymbolConstants.DOUBLE_QUOTATION_CHAR;
import static java.util.function.Function.identity;

/**
 * The meta-data class for the Spring's annotation {@link RequestMapping @RequestMapping}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMapping
 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfo
 * @see org.springframework.web.reactive.result.method.RequestMappingInfo
 * @since 1.0.0
 */
public class RequestMappingMetadata {

    private final String[] patterns;

    private final RequestMethod[] methods;

    private final NameValueExpression<String>[] params;

    private final NameValueExpression<String>[] headers;

    private final MediaTypeExpression[] consumes;

    private final MediaTypeExpression[] produces;

    public RequestMappingMetadata(String[] patterns,
                                  RequestMethod[] methods,
                                  NameValueExpression<String>[] params,
                                  NameValueExpression<String>[] headers,
                                  MediaTypeExpression[] consumes,
                                  MediaTypeExpression[] produces) {
        this.patterns = patterns;
        this.methods = methods;
        this.params = params;
        this.headers = headers;
        this.consumes = consumes;
        this.produces = produces;
    }

    public String[] getPatterns() {
        return patterns;
    }

    public RequestMethod[] getMethods() {
        return methods;
    }

    public NameValueExpression<String>[] getParams() {
        return params;
    }

    public NameValueExpression<String>[] getHeaders() {
        return headers;
    }

    public MediaTypeExpression[] getConsumes() {
        return consumes;
    }

    public MediaTypeExpression[] getProduces() {
        return produces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMappingMetadata that = (RequestMappingMetadata) o;
        return Arrays.equals(patterns, that.patterns)
                && Arrays.equals(methods, that.methods)
                && Arrays.equals(params, that.params)
                && Arrays.equals(headers, that.headers)
                && Arrays.equals(consumes, that.consumes)
                && Arrays.equals(produces, that.produces);
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
        StringBuilder stringBuilder = new StringBuilder("{").append(LINE_SEPARATOR);
        append("patterns", this.patterns, identity(), true, stringBuilder);
        append("methods", this.methods, RequestMethod::name, true, stringBuilder, COMMA, LINE_SEPARATOR);
        append("params", this.params, this::getJSON, false, stringBuilder, COMMA, LINE_SEPARATOR);
        append("headers", this.headers, this::getJSON, false, stringBuilder, COMMA, LINE_SEPARATOR);
        append("consumes", this.consumes, this::getJSON, false, stringBuilder, COMMA, LINE_SEPARATOR);
        append("produces", this.produces, this::getJSON, false, stringBuilder, LINE_SEPARATOR);
        stringBuilder.append(LINE_SEPARATOR).append("}");
        return stringBuilder.toString();
    }

    private String getJSON(NameValueExpression<String> expression) {
        StringBuilder stringBuilder = new StringBuilder("{");
        append("name", expression.getName(), true, stringBuilder);
        append("value", expression.getValue(), true, stringBuilder, COMMA);
        boolean negated = expression.isNegated();
        if (negated) {
            append("negated", expression.isNegated(), false, stringBuilder, COMMA);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private String getJSON(MediaTypeExpression expression) {
        StringBuilder stringBuilder = new StringBuilder("{");
        append("mediaType", expression.getMediaType().toString(), true, stringBuilder);
        boolean negated = expression.isNegated();
        if (negated) {
            append("negated", expression.isNegated(), false, stringBuilder, COMMA);
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private void append(String name, Object value, boolean valueAsString, StringBuilder appendable,
                        String... prefixes) {
        if (value == null) {
            return;
        }
        append(prefixes, appendable);
        append(name, true, appendable);
        appendable.append(COLON_CHAR);
        append(value, valueAsString, appendable);
    }

    private void append(Object appended, boolean asString, StringBuilder appendable) {
        if (appended == null) {
            return;
        }
        if (asString) {
            appendable.append(DOUBLE_QUOTATION_CHAR)
                    .append(appended)
                    .append(DOUBLE_QUOTATION_CHAR);
        } else {
            appendable.append(appended);
        }
    }

    private <R, V> void append(String name, R[] rawValues, Function<R, V> valueConvertor, boolean valueAsString,
                               StringBuilder appendable,
                               String... prefixes) {
        int size = ArrayUtils.size(rawValues);
        if (size < 1) {
            return;
        }

        append(prefixes, appendable);

        append(name, true, appendable);

        appendable.append(COLON_CHAR);

        appendable.append('[');

        for (int i = 0; i < size; i++) {
            R rawValue = rawValues[i];
            V value = valueConvertor.apply(rawValue);
            append(value, valueAsString, appendable);
            if (i != size - 1) {
                appendable.append(COMMA_CHAR);
            }
        }

        appendable.append(']');

    }

    private void append(String[] values, StringBuilder appendable) {
        for (int i = 0; i < values.length; i++) {
            appendable.append(values[i]);
        }
    }
}
