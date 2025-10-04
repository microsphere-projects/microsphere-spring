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
package io.microsphere.spring.webflux.metadata;

import io.microsphere.spring.web.metadata.HandlerMetadata;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.condition.ConsumesRequestCondition;
import org.springframework.web.reactive.result.condition.HeadersRequestCondition;
import org.springframework.web.reactive.result.condition.MediaTypeExpression;
import org.springframework.web.reactive.result.condition.NameValueExpression;
import org.springframework.web.reactive.result.condition.ParamsRequestCondition;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.condition.ProducesRequestCondition;
import org.springframework.web.reactive.result.condition.RequestMethodsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Collection;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newFixedHashSet;
import static java.util.stream.Collectors.toList;

/**
 * {@link WebEndpointMappingFactory} based on Spring WebFlux {@link RequestMappingInfo}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMapping
 * @see RequestMappingInfo
 * @since 1.0.0
 */
public class RequestMappingMetadataWebEndpointMappingFactory extends HandlerMappingWebEndpointMappingFactory<HandlerMethod, RequestMappingInfo> {

    public RequestMappingMetadataWebEndpointMappingFactory(HandlerMapping handlerMapping) {
        super(handlerMapping);
    }

    @Override
    protected HandlerMethod getHandler(HandlerMetadata<HandlerMethod, RequestMappingInfo> handlerMetadata) {
        HandlerMethod handlerMethod = handlerMetadata.getHandler();
        return handlerMethod.createWithResolvedBean();
    }

    @Override
    protected Collection<String> getMethods(HandlerMethod handler, RequestMappingInfo metadata) {
        Set<RequestMethod> requestMethods = metadata.getMethodsCondition().getMethods();
        return requestMethods.stream().map(RequestMethod::name).collect(toList());
    }

    @Override
    protected Collection<String> getPatterns(HandlerMethod handler, RequestMappingInfo requestMappingInfo) {
        return getPatterns(requestMappingInfo);
    }

    @Override
    protected void contribute(HandlerMethod handlerMethod, RequestMappingInfo requestMappingInfo,
                              HandlerMapping handlerMapping, WebEndpointMapping.Builder<HandlerMethod> builder) {

        RequestMethodsRequestCondition methodsCondition = requestMappingInfo.getMethodsCondition();
        ParamsRequestCondition paramsCondition = requestMappingInfo.getParamsCondition();
        HeadersRequestCondition headersCondition = requestMappingInfo.getHeadersCondition();
        ConsumesRequestCondition consumesCondition = requestMappingInfo.getConsumesCondition();
        ProducesRequestCondition producesCondition = requestMappingInfo.getProducesCondition();

        builder.methods(methodsCondition.getMethods(), RequestMethod::name)
                .params(paramsCondition.getExpressions(), NameValueExpression::toString)
                .headers(headersCondition.getExpressions(), NameValueExpression::toString)
                .consumes(consumesCondition.getExpressions(), MediaTypeExpression::toString)
                .produces(producesCondition.getExpressions(), MediaTypeExpression::toString);
    }

    private Set<String> getPatterns(RequestMappingInfo source) {
        PatternsRequestCondition patternsCondition = source.getPatternsCondition();
        Set<PathPattern> pathPatterns = patternsCondition.getPatterns();
        Set<String> patterns = newFixedHashSet(pathPatterns.size());
        for (PathPattern pathPattern : pathPatterns) {
            patterns.add(pathPattern.getPatternString());
        }
        return patterns;
    }

}
