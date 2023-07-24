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
package io.microsphere.spring.webmvc.metadata;

import io.microsphere.spring.web.metadata.WebMappingDescriptor;
import io.microsphere.spring.web.metadata.WebMappingDescriptorFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.MediaTypeExpression;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Set;

import static io.microsphere.spring.web.metadata.WebMappingDescriptor.patterns;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * {@link WebMappingDescriptorFactory} based on Spring WebMVC {@link RequestMappingInfo}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMapping
 * @see RequestMappingInfo
 * @since 1.0.0
 */
public class RequestMappingInfoWebMappingDescriptorFactory implements WebMappingDescriptorFactory<RequestMappingInfo> {

    private static final String CLASS_NAME = "org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition";

    private static final boolean PATH_PATTERNS_REQUEST_CONDITION_CLASS_PRESENT = ClassUtils.isPresent(CLASS_NAME, null);

    @Override
    public WebMappingDescriptor create(RequestMappingInfo source) {

        Set<String> patterns = getPatterns(source);

        if (isEmpty(patterns)) {
            return null;
        }

        RequestMethodsRequestCondition methodsCondition = source.getMethodsCondition();
        ParamsRequestCondition paramsCondition = source.getParamsCondition();
        HeadersRequestCondition headersCondition = source.getHeadersCondition();
        ConsumesRequestCondition consumesCondition = source.getConsumesCondition();
        ProducesRequestCondition producesCondition = source.getProducesCondition();

        return patterns(patterns)
                .methods(methodsCondition.getMethods(), RequestMethod::name)
                .params(paramsCondition.getExpressions(), NameValueExpression::toString)
                .headers(headersCondition.getExpressions(), NameValueExpression::toString)
                .consumes(consumesCondition.getExpressions(), MediaTypeExpression::toString)
                .produces(producesCondition.getExpressions(), MediaTypeExpression::toString)
                .build();
    }

    private Set<String> getPatterns(RequestMappingInfo source) {
        Set<String> patterns = null;
        if (PATH_PATTERNS_REQUEST_CONDITION_CLASS_PRESENT) {
            PathPatternsRequestCondition pathPatternsCondition = source.getPathPatternsCondition();
            if (pathPatternsCondition != null) {
                patterns = pathPatternsCondition.getPatternValues();
            }
        }
        if (isEmpty(patterns)) {
            PatternsRequestCondition patternsCondition = source.getPatternsCondition();
            if (patternsCondition != null) {
                patterns = patternsCondition.getPatterns();
            }
        }
        return patterns;
    }
}
