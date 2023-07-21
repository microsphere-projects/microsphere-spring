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

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.parseMediaTypes;

/**
 * Parses and matches a single media type expression to a request's 'Accept' header.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ProduceMediaTypeExpression extends AbstractMediaTypeExpression {

    public ProduceMediaTypeExpression(String expression) {
        super(expression);
    }

    public ProduceMediaTypeExpression(MediaType mediaType, boolean negated) {
        super(mediaType, negated);
    }

    public final boolean match(List<MediaType> acceptedMediaTypes) {
        boolean match = matchMediaType(acceptedMediaTypes);
        return !isNegated() == match;
    }

    private boolean matchMediaType(List<MediaType> acceptedMediaTypes) {
        for (MediaType acceptedMediaType : acceptedMediaTypes) {
            if (getMediaType().isCompatibleWith(acceptedMediaType) && matchParameters(acceptedMediaType)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchParameters(MediaType acceptedMediaType) {
        for (String name : getMediaType().getParameters().keySet()) {
            String s1 = getMediaType().getParameter(name);
            String s2 = acceptedMediaType.getParameter(name);
            if (StringUtils.hasText(s1) && StringUtils.hasText(s2) && !s1.equalsIgnoreCase(s2)) {
                return false;
            }
        }
        return true;
    }

    public static List<ProduceMediaTypeExpression> parseExpressions(String[] produces, @Nullable String[] headers) {
        Set<ProduceMediaTypeExpression> result = null;
        if (!ObjectUtils.isEmpty(headers)) {
            for (String header : headers) {
                WebRequestHeaderExpression expression = new WebRequestHeaderExpression(header);
                if (ACCEPT.equalsIgnoreCase(expression.name) && expression.value != null) {
                    List<MediaType> mediaTypes = parseMediaTypes(expression.value);
                    for (MediaType mediaType : mediaTypes) {
                        result = (result != null ? result : new LinkedHashSet<>());
                        result.add(new ProduceMediaTypeExpression(mediaType, expression.isNegated));
                    }
                }
            }
        }
        if (!ObjectUtils.isEmpty(produces)) {
            for (String produce : produces) {
                result = (result != null ? result : new LinkedHashSet<>());
                result.add(new ProduceMediaTypeExpression(produce));
            }
        }
        return (result != null ? new ArrayList<>(result) : Collections.emptyList());
    }
}
