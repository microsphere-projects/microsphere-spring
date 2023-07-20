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
package io.microsphere.spring.web.rule;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.WebUtils;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.net.URLUtils.resolveQueryParameters;
import static java.util.Collections.emptySet;

/**
 * {@link HttpRequest} Header {@link NameValueExpression}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NameValueExpression
 * @since 1.0.0
 */
public class HttpRequestHeaderExpression extends AbstractNameValueExpression {

    private final Set<String> namesToMatch = new HashSet<>(WebUtils.SUBMIT_IMAGE_SUFFIXES.length + 1);

    public HttpRequestHeaderExpression(String expression) {
        super(expression);
        this.namesToMatch.add(getName());
        for (String suffix : WebUtils.SUBMIT_IMAGE_SUFFIXES) {
            this.namesToMatch.add(getName() + suffix);
        }
    }

    @Override
    protected boolean isCaseSensitiveName() {
        return false;
    }

    @Override
    protected Object parseValue(String valueExpression) {
        return valueExpression;
    }

    @Override
    protected boolean matchName(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.containsKey(this.name);
    }

    @Override
    protected boolean matchValue(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        List<String> values = headers.getValuesAsList(this.name);
        return values != null && values.contains(this.value);
    }

    protected static Set<HttpRequestHeaderExpression> of(String... headers) {
        Set<HttpRequestHeaderExpression> result = null;
        if (!ObjectUtils.isEmpty(headers)) {
            for (String header : headers) {
                HttpRequestHeaderExpression expression = new HttpRequestHeaderExpression(header);
                if ("Accept".equalsIgnoreCase(expression.name) || "Content-Type".equalsIgnoreCase(expression.name)) {
                    continue;
                }
                result = (result != null ? result : new LinkedHashSet<>(headers.length));
                result.add(expression);
            }
        }
        return (result != null ? result : emptySet());
    }
}
