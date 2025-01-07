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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.microsphere.util.ArrayUtils.isEmpty;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.parseMediaTypes;

/**
 * Parses and matches a single media type expression to a request's 'Content-Type' header.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition.ConsumeMediaTypeExpression
 * @since 1.0.0
 */
public class ConsumeMediaTypeExpression extends GenericMediaTypeExpression {

    public ConsumeMediaTypeExpression(String expression) {
        super(expression);
    }

    public ConsumeMediaTypeExpression(MediaType mediaType, boolean negated) {
        super(mediaType, negated);
    }

    public final boolean match(MediaType contentType) {
        boolean match = getMediaType().includes(contentType);
        return !isNegated() == match;
    }


    public static List<ConsumeMediaTypeExpression> parseExpressions(String[] consumes, @Nullable String[] headers) {
        Set<ConsumeMediaTypeExpression> result = null;
        if (!isEmpty(headers)) {
            for (String header : headers) {
                WebRequestHeaderExpression expression = new WebRequestHeaderExpression(header);
                if (CONTENT_TYPE.equalsIgnoreCase(expression.name) && expression.value != null) {
                    result = (result != null ? result : new LinkedHashSet<>());
                    List<MediaType> mediaTypes = parseMediaTypes(expression.value);
                    for (MediaType mediaType : mediaTypes) {
                        result.add(new ConsumeMediaTypeExpression(mediaType, expression.isNegated));
                    }
                }
            }
        }
        if (!isEmpty(consumes)) {
            result = (result != null ? result : new LinkedHashSet<>());
            for (String consume : consumes) {
                result.add(new ConsumeMediaTypeExpression(consume));
            }
        }
        return (result != null ? new ArrayList<>(result) : Collections.emptyList());
    }
}
