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
package io.microsphere.spring.webflux.handler;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webflux.metadata.HandlerMetadataWebEndpointMappingFactory;
import io.microsphere.spring.webflux.metadata.RequestMappingMetadataWebEndpointMappingFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.reactive.handler.AbstractUrlHandlerMapping;
import org.springframework.web.reactive.result.method.AbstractHandlerMethodMapping;
import org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.ID_HEADER_NAME;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_FLUX;
import static io.microsphere.util.StringUtils.isBlank;
import static java.lang.String.valueOf;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

/**
 * The performance optimization {@link HandlerMapping} to process the forwarded request
 * from the reversed proxy web server, e.g, Spring Cloud Netflix Zuul, Spring Cloud Gateway or others.
 * The request must have a header named {@link WebEndpointMapping#ID_HEADER_NAME "microsphere_wem_id"},
 * which is a string presenting {@link WebEndpointMapping#getId() the id of endpoint}, used to
 * locate the actual {@link WebEndpointMapping#getEndpoint() endpoint} easily, such as {@link HandlerMethod},
 * {@link org.springframework.web.reactive.function.server.HandlerFunction}.
 * <p>
 * As a result, {@link ReversedProxyHandlerMapping} has the higher precedence than others, which ensures that it
 * prioritizes {@link HandlerMapping#getHandler(ServerWebExchange) getting the handler} and avoid the duplication
 * that was executed by the reversed proxy.
 * As regards the details of {@link WebEndpointMapping#getEndpoint() endpoint}, it's recommended to read the JavaDoc of
 * {@link WebEndpointMapping#getEndpoint()}.
 *
 * <p>
 * For now, {@link ReversedProxyHandlerMapping} only supports to get the handlers from
 * {@link RequestMappingHandlerMapping}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMapping
 * @see AbstractUrlHandlerMapping
 * @see AbstractHandlerMethodMapping
 * @see HandlerMetadataWebEndpointMappingFactory
 * @see RequestMappingMetadataWebEndpointMappingFactory
 * @see RequestMappingInfoHandlerMapping
 * @since 1.0.0
 */
public class ReversedProxyHandlerMapping extends AbstractHandlerMapping implements ApplicationListener<WebEndpointMappingsReadyEvent> {

    private static final Logger logger = getLogger(ReversedProxyHandlerMapping.class);

    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;

    private Map<String, WebEndpointMapping> webEndpointMappingsCache;

    public ReversedProxyHandlerMapping() {
        setOrder(DEFAULT_ORDER);
    }

    @Override
    protected Mono<?> getHandlerInternal(ServerWebExchange serverWebExchange) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        String webEndpointMappingId = request.getHeaders().getFirst(ID_HEADER_NAME);
        if (isBlank(webEndpointMappingId)) {
            logger.trace("No request header of the WebEndpointMapping ID [name : '{}'] is present", ID_HEADER_NAME);
            return empty();
        }
        WebEndpointMapping webEndpointMapping = webEndpointMappingsCache.get(webEndpointMappingId);
        if (webEndpointMapping == null) {
            logger.trace("No WebEndpointMapping was found by the request header[name : '{}' , value : {}]", ID_HEADER_NAME, webEndpointMappingId);
            return empty();
        }
        return getHandlerInternal(webEndpointMapping);
    }

    @Nullable
    protected Mono<?> getHandlerInternal(WebEndpointMapping webEndpointMapping) {
        if (isAbstractHandlerMapping(webEndpointMapping)) {
            Object handler = webEndpointMapping.getEndpoint();
            return just(handler);
        }
        return empty();
    }

    @Override
    public void onApplicationEvent(WebEndpointMappingsReadyEvent event) {
        Collection<WebEndpointMapping> webEndpointMappings = event.getMappings();
        int size = webEndpointMappings.size();
        Map<String, WebEndpointMapping> webEndpointMappingsMap = newFixedHashMap(size);
        event.getMappings().stream()
                .filter(this::isAbstractHandlerMapping)
                .forEach(mapping -> webEndpointMappingsMap.put(valueOf(mapping.getId()), mapping));

        this.webEndpointMappingsCache = webEndpointMappingsMap;
    }

    private boolean isAbstractHandlerMapping(WebEndpointMapping webEndpointMapping) {
        return WEB_FLUX.equals(webEndpointMapping.getKind()) &&
                webEndpointMapping.getSource() instanceof AbstractHandlerMapping;
    }
}
