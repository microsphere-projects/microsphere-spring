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

import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;
import java.util.List;

import static io.microsphere.spring.web.rule.WebRequestParamExpression.parseExpressions;

/**
 * A {@link WebRequestRule} that matches requests based on parameter expressions.
 *
 * <p>This class extends {@link AbstractWebRequestRule} with support for
 * {@link WebRequestParamExpression} to define conditions on request parameters.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a rule that matches requests with the parameter "foo" set to "bar"
 * WebRequestParamsRule rule = new WebRequestParamsRule("foo=bar");
 *
 * // Create a rule that matches requests with both "foo=bar" and "baz"
 * WebRequestParamsRule rule = new WebRequestParamsRule("foo=bar", "baz");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestParamExpression
 * @see AbstractWebRequestRule
 * @see org.springframework.web.servlet.mvc.condition.ParamsRequestCondition
 * @see org.springframework.web.reactive.result.condition.ParamsRequestCondition
 * @since 1.0.0
 */
public class WebRequestParamsRule extends AbstractWebRequestRule<WebRequestParamExpression> {

    private final List<WebRequestParamExpression> expressions;

    public WebRequestParamsRule(String... params) {
        this.expressions = parseExpressions(params);
    }

    @Override
    protected Collection<WebRequestParamExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            WebRequestParamExpression expression = expressions.get(i);
            if (!expression.match(request)) {
                return false;
            }
        }
        return true;
    }
}
