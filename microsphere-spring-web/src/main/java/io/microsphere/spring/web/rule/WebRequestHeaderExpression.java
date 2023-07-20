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

import io.microsphere.util.ArrayUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.WebUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.ObjectUtils.containsElement;

/**
 * {@link NativeWebRequest} Header {@link NameValueExpression}
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NameValueExpression
 * @since 1.0.0
 */
public class WebRequestHeaderExpression extends AbstractNameValueExpression {

    private final Set<String> namesToMatch = new HashSet<>(WebUtils.SUBMIT_IMAGE_SUFFIXES.length + 1);

    public WebRequestHeaderExpression(String expression) {
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
    protected boolean matchName(NativeWebRequest request) {
        return request.getHeader(this.name) != null;
    }

    @Override
    protected boolean matchValue(NativeWebRequest request) {
        String[] values = request.getHeaderValues(this.name);
        return containsElement(values, this.value);
    }

    protected static List<WebRequestHeaderExpression> parseExpressions(String... headers) {
        List<WebRequestHeaderExpression> expressions = emptyList();
        int size = ArrayUtils.size(headers);
        if (size > 0) {
            expressions = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                String header = headers[i];
                WebRequestHeaderExpression expression = new WebRequestHeaderExpression(header);
                if (ACCEPT.equalsIgnoreCase(expression.name) || CONTENT_TYPE.equalsIgnoreCase(expression.name)) {
                    continue;
                }
                expressions.add(expression);
            }
        }
        return expressions;
    }
}
