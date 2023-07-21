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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;
import java.util.List;

import static io.microsphere.spring.web.rule.WebRequestHeaderExpression.parseExpressions;

/**
 * {@link NativeWebRequest WebRequest} Headers {@link WebRequestRule}
 * <p>
 * A logical conjunction ({@code ' && '}) request condition that matches a request against
 * a set of header expressions with syntax defined in {@link RequestMapping#headers()}.
 *
 * <p>Expressions passed to the constructor with header names 'Accept' or
 * 'Content-Type' are ignored.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestRule
 * @see org.springframework.web.servlet.mvc.condition.HeadersRequestCondition
 * @since 1.0.066
 */
public class WebRequestHeadersRule extends AbstractWebRequestRule<WebRequestHeaderExpression> {

    private final List<WebRequestHeaderExpression> expressions;

    public WebRequestHeadersRule(String... params) {
        this.expressions = parseExpressions(params);
    }

    @Override
    protected Collection<WebRequestHeaderExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        for (WebRequestHeaderExpression expression : expressions) {
            if (!expression.match(request)) {
                return false;
            }
        }
        return true;
    }
}
