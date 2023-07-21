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

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;
import java.util.List;

import static io.microsphere.spring.web.rule.ConsumeMediaTypeExpression.parseExpressions;
import static io.microsphere.spring.web.util.WebRequestUtils.getContentType;
import static io.microsphere.spring.web.util.WebRequestUtils.isPreFlightRequest;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.util.StringUtils.hasLength;


/**
 * {@link NativeWebRequest WebRequest} Consumes {@link WebRequestRule}
 * <p>
 * A logical disjunction (' || ') request condition to match a request's
 * 'Content-Type' header to a list of media type expressions. Two kinds of
 * media type expressions are supported, which are described in
 * {@link RequestMapping#consumes()} and {@link RequestMapping#headers()}
 * where the header name is 'Content-Type'. Regardless of which syntax is
 * used, the semantics are the same.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestRule
 * @see org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition
 * @since 1.0.066
 */
public class WebRequestConsumesRule extends AbstractWebRequestRule<ConsumeMediaTypeExpression> {

    private final List<ConsumeMediaTypeExpression> expressions;

    public WebRequestConsumesRule(String... consumes) {
        this(consumes, null);
    }

    public WebRequestConsumesRule(String[] consumes, String... headers) {
        this.expressions = parseExpressions(consumes, headers);
    }

    @Override
    protected Collection<ConsumeMediaTypeExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        if (isPreFlightRequest(request)) {
            return false;
        }
        if (isEmpty()) {
            return false;
        }

        MediaType contentType;
        try {
            String contentTypeValue = getContentType(request);
            contentType = hasLength(contentTypeValue) ? parseMediaType(contentTypeValue) : APPLICATION_OCTET_STREAM;
        } catch (InvalidMediaTypeException ex) {
            return false;
        }

        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            ConsumeMediaTypeExpression expression = expressions.get(i);
            if (!expression.match(contentType)) {
                return false;
            }
        }
        return true;
    }
}
