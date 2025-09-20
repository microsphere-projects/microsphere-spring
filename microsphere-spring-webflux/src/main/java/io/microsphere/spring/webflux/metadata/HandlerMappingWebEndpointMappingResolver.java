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
import static io.microsphere.collection.MapUtils.newFixedHashMap;
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
            HandlerMetadataWebEndpointMappingFactory factory = new HandlerMetadataWebEndpointMappingFactory(urlHandlerMapping);
            Map<String, Object> newHandlerMap = buildNewHandlerMap(handlerMap);
            resolveWebEndpointMappings(newHandlerMap, factory, webEndpointMappings);
        }
    }

    protected void resolveFromRequestMappingInfoHandlerMapping(HandlerMapping handlerMapping,
                                                               Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods,
                                                               List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
            RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethodsMap = requestMappingInfoHandlerMapping.getHandlerMethods();
            RequestMappingMetadataWebEndpointMappingFactory factory = new RequestMappingMetadataWebEndpointMappingFactory(requestMappingInfoHandlerMapping);
            resolveWebEndpointMappings(handlerMethodsMap, factory, webEndpointMappings);
            requestMappingInfoHandlerMethods.putAll(handlerMethodsMap);
        }
    }

    protected void resolveFromRouterFunctionMapping(HandlerMapping handlerMapping, List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof RouterFunctionMapping) {
            RouterFunctionMapping routerFunctionMapping = (RouterFunctionMapping) handlerMapping;
            RouterFunction<?> routerFunction = routerFunctionMapping.getRouterFunction();
            if (routerFunction != null) {
                routerFunction.accept(new ConsumingWebEndpointMappingAdapter(webEndpointMappings::add, routerFunctionMapping));
            }
        }
    }

    Map<String, Object> buildNewHandlerMap(Map<PathPattern, Object> handlerMap) {
        Map<String, Object> newHandlerMap = newFixedHashMap(handlerMap.size());
        for (Entry<PathPattern, Object> entry : handlerMap.entrySet()) {
            PathPattern pathPattern = entry.getKey();
            Object handler = entry.getValue();
            newHandlerMap.put(pathPattern.getPatternString(), handler);
        }
        return newHandlerMap;
    }

    <H, M> void resolveWebEndpointMappings(Map<M, H> map, WebEndpointMappingFactory<HandlerMetadata<H, M>> factory,
                                           List<WebEndpointMapping> webEndpointMappings) {
        for (Entry<M, H> entry : map.entrySet()) {
            H handler = entry.getValue();
            M metadata = entry.getKey();
            HandlerMetadata<H, M> handlerMetadata = new HandlerMetadata<>(handler, metadata);
            if (factory.supports(handlerMetadata)) {
                Optional<WebEndpointMapping<HandlerMetadata<H, M>>> webEndpointMapping = factory.create(handlerMetadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
        }
    }
}