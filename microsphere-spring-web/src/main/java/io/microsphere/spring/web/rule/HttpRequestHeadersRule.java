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

import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.Set;

/**
 * {@link HttpRequest} Headers {@link HttpRequestRule}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HttpRequestRule
 * @see org.springframework.web.servlet.mvc.condition.HeadersRequestCondition
 * @since 1.0.066
 */
public class HttpRequestHeadersRule extends AbstractHttpRequestRule<HttpRequestHeadersRule> {

    private final Set<HttpRequestHeaderExpression> expressions;

    public HttpRequestHeadersRule(String... params) {
        this.expressions = HttpRequestHeaderExpression.of(params);
    }

    @Override
    protected Collection<HttpRequestHeaderExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    @Override
    public HttpRequestHeadersRule getMatchingRule(HttpRequest request) {
        for (HttpRequestHeaderExpression expression : expressions) {
            if (!expression.match(request)) {
                return null;
            }
        }
        return this;
    }
}
