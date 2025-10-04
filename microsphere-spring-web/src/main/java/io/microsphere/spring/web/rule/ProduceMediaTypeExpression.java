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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptyList;
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
public class ProduceMediaTypeExpression extends GenericMediaTypeExpression {

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

    boolean matchMediaType(List<MediaType> acceptedMediaTypes) {
        for (MediaType acceptedMediaType : acceptedMediaTypes) {
            if (getMediaType().isCompatibleWith(acceptedMediaType) && matchParameters(acceptedMediaType)) {
                return true;
            }
        }
        return false;
    }

    public static List<ProduceMediaTypeExpression> parseExpressions(String[] produces, @Nullable String[] headers) {
        int producesSize = length(produces);
        int headersSize = length(headers);

        Set<ProduceMediaTypeExpression> result = newFixedLinkedHashSet(producesSize + headersSize);

        for (int i = 0; i < headersSize; i++) {
            String header = headers[i];
            WebRequestHeaderExpression expression = new WebRequestHeaderExpression(header);
            if (ACCEPT.equalsIgnoreCase(expression.name) && expression.value != null) {
                List<MediaType> mediaTypes = parseMediaTypes(expression.value);
                for (MediaType mediaType : mediaTypes) {
                    result.add(new ProduceMediaTypeExpression(mediaType, expression.isNegated));
                }
            }
        }

        for (int i = 0; i < producesSize; i++) {
            String produce = produces[i];
            result.add(new ProduceMediaTypeExpression(produce));
        }

        return result.isEmpty() ? emptyList() : new ArrayList<>(result);
    }
}
