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

import io.microsphere.annotation.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.core.SpringVersion.CURRENT;
import static io.microsphere.spring.core.SpringVersion.SPRING_5_0_5;
import static io.microsphere.spring.util.MimeTypeUtils.isPresentIn;
import static io.microsphere.spring.web.rule.ProduceMediaTypeExpression.parseExpressions;
import static io.microsphere.spring.web.util.WebRequestUtils.isPreFlightRequest;
import static java.util.Collections.sort;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;


/**
 * {@link NativeWebRequest WebRequest} Produces {@link WebRequestRule}
 * <p>
 * A logical disjunction (' || ') request condition to match a request's 'Accept' header
 * to a list of media type expressions. Two kinds of media type expressions are
 * supported, which are described in {@link RequestMapping#produces()} and
 * {@link RequestMapping#headers()} where the header name is 'Accept'.
 * Regardless of which syntax is used, the semantics are the same.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestRule
 * @see org.springframework.web.servlet.mvc.condition.ProducesRequestCondition
 * @see org.springframework.web.reactive.result.condition.ProducesRequestCondition
 * @since 1.0.0
 */
public class WebRequestProducesRule extends AbstractWebRequestRule<ProduceMediaTypeExpression> {

    static final boolean BEFORE_SPRING_505 = CURRENT.isLessThan(SPRING_5_0_5);
    /**
     * A singleton list with {@link MediaType#ALL} that is returned from
     * {@link ContentNegotiationStrategy#resolveMediaTypes} when no specific media types are requested.
     *
     * @since Spring Framework 5.0.5
     */
    static final List<MediaType> MEDIA_TYPE_ALL_LIST = ofList(ALL);

    private static final ContentNegotiationManager DEFAULT_CONTENT_NEGOTIATION_MANAGER =
            new ContentNegotiationManager();

    // private static final List<ProduceMediaTypeExpression> MEDIA_TYPE_ALL_LIST = ofList(new ProduceMediaTypeExpression(ALL_VALUE));

    private static final String MEDIA_TYPES_ATTRIBUTE = WebRequestProducesRule.class.getName() + ".MEDIA_TYPES";

    private final List<ProduceMediaTypeExpression> expressions;

    private final ContentNegotiationManager contentNegotiationManager;

    public WebRequestProducesRule(String... produces) {
        this(produces, null);
    }

    public WebRequestProducesRule(String[] produces, @Nullable String... headers) {
        this(produces, headers, null);
    }

    public WebRequestProducesRule(String[] produces, @Nullable String[] headers,
                                  @Nullable ContentNegotiationManager manager) {
        this.expressions = parseExpressions(produces, headers);
        if (this.expressions.size() > 1) {
            sort(this.expressions);
        }
        this.contentNegotiationManager = manager != null ? manager : DEFAULT_CONTENT_NEGOTIATION_MANAGER;
    }

    @Override
    protected Collection<ProduceMediaTypeExpression> getContent() {
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

        List<MediaType> acceptedMediaTypes;
        try {
            acceptedMediaTypes = getAcceptedMediaTypes(request);
        } catch (HttpMediaTypeException ex) {
            return false;
        }
        List<ProduceMediaTypeExpression> result = getMatchingExpressions(acceptedMediaTypes);
        if (isNotEmpty(result)) {
            return false;
        }
        return !isPresentIn(ALL, acceptedMediaTypes);
    }

    private List<ProduceMediaTypeExpression> getMatchingExpressions(List<MediaType> acceptedMediaTypes) {
        List<ProduceMediaTypeExpression> result = newArrayList(this.expressions.size());
        for (ProduceMediaTypeExpression expression : this.expressions) {
            if (expression.match(acceptedMediaTypes)) {
                result.add(expression);
            }
        }
        return result;
    }

    private List<MediaType> getAcceptedMediaTypes(NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {
        List<MediaType> result = (List<MediaType>) request.getAttribute(MEDIA_TYPES_ATTRIBUTE, SCOPE_REQUEST);
        if (result == null) {
            // The return value of ContentNegotiationStrategy#resolveMediaTypes(NativeWebRequest) had been changed
            // since Spring Framework 5.0.5 : the requested media types, or MEDIA_TYPE_ALL_LIST if none were requested.
            result = this.contentNegotiationManager.resolveMediaTypes(request);
            if (result.isEmpty() && BEFORE_SPRING_505) {
                result = MEDIA_TYPE_ALL_LIST;
            }
            request.setAttribute(MEDIA_TYPES_ATTRIBUTE, result, SCOPE_REQUEST);
        }
        return result;
    }

}
