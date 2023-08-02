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
package io.microsphere.spring.webmvc.handler;

import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webmvc.metadata.HandlerMetadataWebEndpointMappingFactory;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataWebEndpointMappingFactory;
import io.microsphere.spring.webmvc.metadata.WebEndpointMappingsReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.ID_HEADER_NAME;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;

/**
 * The Spring WebMVC {@link HandlerMapping} for Reversed Proxy, e.g,
 * Spring Cloud Netflix Zuul, Spring Cloud Gateway , other reversed proxy web server.
 * The HTTP request must be forwarded by the reversed proxy web server, which contains
 * the specified header named {@link WebEndpointMapping#ID_HEADER_NAME}, {@link ReversedProxyHandlerMapping}
 * will process this header and then locate the identified handler to response the request.
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

    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;

    private Map<Integer, WebEndpointMapping> webEndpointMappingsCache;

    public ReversedProxyHandlerMapping() {
        setOrder(DEFAULT_ORDER);
    }

    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        int webEndpointMappingId = request.getIntHeader(ID_HEADER_NAME);
        if (webEndpointMappingId == -1) {
            // No WebEndpointMapping ID Header present
            return null;
        }
        WebEndpointMapping webEndpointMapping = webEndpointMappingsCache.get(webEndpointMappingId);
        if (webEndpointMapping == null) {
            return null;
        }

        Object source = webEndpointMapping.getEndpoint();
        if (source instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) source;
            return handlerMethod.createWithResolvedBean();
        }
        return source;
    }

    @Override
    public void onApplicationEvent(WebEndpointMappingsReadyEvent event) {
        Map<Integer, WebEndpointMapping> webEndpointMappingsMap = new HashMap<>();
        event.getMappings().stream()
                .filter(this::isWebMvcWebEndpointMapping)
                .forEach(webEndpointMapping -> {
                    webEndpointMappingsMap.put(webEndpointMapping.getId(), webEndpointMapping);
                });

        this.webEndpointMappingsCache = webEndpointMappingsMap;
    }

    private boolean isWebMvcWebEndpointMapping(WebEndpointMapping webEndpointMapping) {
        return WEB_MVC.equals(webEndpointMapping.getKind());
    }

}
