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
import io.microsphere.spring.web.metadata.HandlerMethodMetadata;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingResolver;
import io.microsphere.spring.webflux.function.server.ConsumingWebEndpointMappingAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.handler.AbstractUrlHandlerMapping;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * {@link WebEndpointMappingResolver} based on {@link HandlerMapping}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMapping
 * @see AbstractUrlHandlerMapping
 * @see RequestMappingInfoHandlerMapping
 * @since 1.0.0
 */
public class HandlerMappingWebEndpointMappingResolver implements WebEndpointMappingResolver {

    @Override
    public Collection<WebEndpointMapping> resolve(ApplicationContext context) {
        List<WebEndpointMapping> webEndpointMappings = newLinkedList();
        Map<String, HandlerMapping> handlerMappingsMap = beansOfTypeIncludingAncestors(context, HandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods = newHashMap();

        for (HandlerMapping handlerMapping : handlerMappingsMap.values()) {
            resolveFromAbstractUrlHandlerMapping(handlerMapping, webEndpointMappings);
            resolveFromRequestMappingInfoHandlerMapping(handlerMapping, requestMappingInfoHandlerMethods, webEndpointMappings);
            resolveFromRouterFunctionMapping(handlerMapping, webEndpointMappings);
        }
        return webEndpointMappings;
    }

    protected void resolveFromAbstractUrlHandlerMapping(HandlerMapping handlerMapping, List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof AbstractUrlHandlerMapping) {
            AbstractUrlHandlerMapping urlHandlerMapping = (AbstractUrlHandlerMapping) handlerMapping;
            Map<PathPattern, Object> handlerMap = urlHandlerMapping.getHandlerMap();
            if (handlerMap.isEmpty()) {
                return;
            }

            HandlerMetadataWebEndpointMappingFactory factory = new HandlerMetadataWebEndpointMappingFactory(urlHandlerMapping);
            for (Entry<PathPattern, Object> entry : handlerMap.entrySet()) {
                PathPattern pathPattern = entry.getKey();
                Object handler = entry.getValue();
                HandlerMetadata<Object, String> metadata = new HandlerMetadata<>(handler, pathPattern.getPatternString());
                Optional<WebEndpointMapping<HandlerMetadata<Object, String>>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
        }
    }

    protected void resolveFromRequestMappingInfoHandlerMapping(HandlerMapping handlerMapping,
                                                               Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods,
                                                               List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
            RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethodsMap = requestMappingInfoHandlerMapping.getHandlerMethods();
            if (handlerMethodsMap.isEmpty()) {
                return;
            }

            RequestMappingMetadataWebEndpointMappingFactory factory = new RequestMappingMetadataWebEndpointMappingFactory(requestMappingInfoHandlerMapping);
            for (Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodsMap.entrySet()) {
                HandlerMethodMetadata<RequestMappingInfo> metadata = new HandlerMethodMetadata<>(entry.getValue(), entry.getKey());
                Optional<WebEndpointMapping<HandlerMetadata<HandlerMethod, RequestMappingInfo>>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
            requestMappingInfoHandlerMethods.putAll(handlerMethodsMap);
        }
    }

    protected void resolveFromRouterFunctionMapping(HandlerMapping handlerMapping, List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof RouterFunctionMapping) {
            RouterFunctionMapping routerFunctionMapping = (RouterFunctionMapping) handlerMapping;
            RouterFunction<?> routerFunction = routerFunctionMapping.getRouterFunction();
            if (routerFunction != null) {
                routerFunction.accept(new ConsumingWebEndpointMappingAdapter(webEndpointMappings::add, routerFunction));
            }
        }
    }
}
