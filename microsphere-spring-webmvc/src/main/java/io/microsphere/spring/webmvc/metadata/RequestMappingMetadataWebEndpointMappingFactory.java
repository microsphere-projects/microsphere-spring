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

import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.MediaTypeExpression;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Set;

import static io.microsphere.invoke.MethodHandleUtils.NOT_FOUND_METHOD_HANDLE;
import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * {@link WebEndpointMappingFactory} based on Spring WebMVC {@link RequestMappingInfo}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestMapping
 * @see RequestMappingInfo
 * @since 1.0.0
 */
public class RequestMappingMetadataWebEndpointMappingFactory extends HandlerMappingWebEndpointMappingFactory<HandlerMethod, RequestMappingInfo> {

    /**
     * The method name of {@link RequestMappingInfo#getPatternValues()}
     *
     * @since Spring Framework 5.3
     */
    private static final String GET_PATTERNS_VALUES_METHOD_NAME = "getPatternValues";

    /**
     * The {@link MethodHandle} of {@link RequestMappingInfo#getPatternValues()}
     *
     * @since Spring Framework 5.3
     */
    private static final MethodHandle GET_PATTERNS_VALUES_METHOD_HANDLE = findVirtual(RequestMappingInfo.class, GET_PATTERNS_VALUES_METHOD_NAME);

    public RequestMappingMetadataWebEndpointMappingFactory(HandlerMapping handlerMapping) {
        super(handlerMapping);
    }

    @Override
    protected HandlerMethod getHandler(HandlerMetadata<HandlerMethod, RequestMappingInfo> handlerMetadata) {
        HandlerMethod handlerMethod = handlerMetadata.getHandler();
        return handlerMethod.createWithResolvedBean();
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
        Set<String> patterns = null;
        if (GET_PATTERNS_VALUES_METHOD_HANDLE != NOT_FOUND_METHOD_HANDLE) {
            try {
                patterns = (Set<String>) GET_PATTERNS_VALUES_METHOD_HANDLE.invokeExact(source);
            } catch (Throwable e) {
                logger.error("RequestMappingInfo.getPatternValues() can't be invoked by {}", GET_PATTERNS_VALUES_METHOD_HANDLE, e);
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
