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

import io.microsphere.spring.context.lifecycle.AbstractSmartLifecycle;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.FilterRegistrationWebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.ServletRegistrationWebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.microsphere.enterprise.servlet.enumeration.ServletVersion.SERVLET_3_0;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * The class registers all instances of {@link WebEndpointMapping} that are
 * collected from Spring Web MVC and Servlet components into {@link WebEndpointMappingRegistry}
 * before {@link WebEventPublisher} publishing the {@link WebEndpointMappingsReadyEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingRegistry
 * @see WebEventPublisher
 * @see AbstractSmartLifecycle
 * @since 1.0.0
 */
public class WebEndpointMappingRegistrar extends AbstractSmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(WebEndpointMappingRegistrar.class);

    private final WebApplicationContext context;

    public WebEndpointMappingRegistrar(WebApplicationContext context) {
        this.context = context;
        // Mark sure earlier than WebEventPublisher
        setPhase(WebEventPublisher.DEFAULT_PHASE - 10);
    }

    @Override
    protected void doStart() {
        registerWebEndpointMappings();
    }

    private void registerWebEndpointMappings() {
        WebEndpointMappingRegistry registry = getRegistry();
        Collection<WebEndpointMapping> webEndpointMappings = collectWebEndpointMappings();
        int count = registry.register(webEndpointMappings);
        logger.info("{} WebEndpointMappings were registered from the Spring context[id :'{}']", count, context.getId());
    }

    private WebEndpointMappingRegistry getRegistry() {
        return context.getBean(WebEndpointMappingRegistry.class);
    }

    private Collection<WebEndpointMapping> collectWebEndpointMappings() {
        Map<String, HandlerMapping> handlerMappingsMap = beansOfTypeIncludingAncestors(context, HandlerMapping.class);

        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods = new HashMap<>();
        List<WebEndpointMapping> webEndpointMappings = new LinkedList<>();

        ServletContext servletContext = context.getServletContext();

        if (SERVLET_3_0.le(servletContext)) { // Servlet 3.0+
            collectFromServletContext(servletContext, context, webEndpointMappings);
        }

        for (HandlerMapping handlerMapping : handlerMappingsMap.values()) {
            collectFromAbstractUrlHandlerMapping(handlerMapping, webEndpointMappings);
            collectFromRequestMappingInfoHandlerMapping(handlerMapping, requestMappingInfoHandlerMethods, webEndpointMappings);
        }

        return webEndpointMappings;
    }

    private void collectFromServletContext(ServletContext servletContext, WebApplicationContext context,
                                           List<WebEndpointMapping> webEndpointMappings) {
        collectFromFilters(servletContext, context, webEndpointMappings);
        collectFromServlets(servletContext, context, webEndpointMappings);
    }

    private void collectFromFilters(ServletContext servletContext, WebApplicationContext context,
                                    List<WebEndpointMapping> webEndpointMappings) {
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        if (filterRegistrations.isEmpty()) {
            return;
        }

        FilterRegistrationWebEndpointMappingFactory factory = new FilterRegistrationWebEndpointMappingFactory(servletContext);
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            String filterName = entry.getKey();
            Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create(filterName);
            webEndpointMapping.ifPresent(webEndpointMappings::add);
        }
    }

    private void collectFromServlets(ServletContext servletContext, WebApplicationContext context,
                                     List<WebEndpointMapping> webEndpointMappings) {
        Map<String, ? extends ServletRegistration> servletRegistrations = servletContext.getServletRegistrations();
        if (servletRegistrations.isEmpty()) {
            return;
        }

        ServletRegistrationWebEndpointMappingFactory factory = new ServletRegistrationWebEndpointMappingFactory(servletContext);
        for (Map.Entry<String, ? extends ServletRegistration> entry : servletRegistrations.entrySet()) {
            String servletName = entry.getKey();
            Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create(servletName);
            webEndpointMapping.ifPresent(webEndpointMappings::add);
        }
    }

    private void collectFromAbstractUrlHandlerMapping(HandlerMapping handlerMapping, List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof AbstractUrlHandlerMapping) {
            AbstractUrlHandlerMapping urlHandlerMapping = (AbstractUrlHandlerMapping) handlerMapping;
            Map<String, Object> handlerMap = urlHandlerMapping.getHandlerMap();
            if (handlerMap.isEmpty()) {
                return;
            }

            HandlerMetadataWebEndpointMappingFactory factory = new HandlerMetadataWebEndpointMappingFactory(urlHandlerMapping);
            for (Map.Entry<String, Object> entry : handlerMap.entrySet()) {
                HandlerMetadata<Object, String> metadata = new HandlerMetadata<>(entry.getValue(), entry.getKey());
                Optional<WebEndpointMapping<HandlerMetadata<Object, String>>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
        }
    }

    private void collectFromRequestMappingInfoHandlerMapping(HandlerMapping handlerMapping,
                                                             Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods,
                                                             List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
            RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethodsMap = requestMappingInfoHandlerMapping.getHandlerMethods();
            if (handlerMethodsMap.isEmpty()) {
                return;
            }

            RequestMappingMetadataWebEndpointMappingFactory factory = new RequestMappingMetadataWebEndpointMappingFactory(requestMappingInfoHandlerMapping);
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodsMap.entrySet()) {
                RequestMappingMetadata metadata = new RequestMappingMetadata(entry.getKey(), entry.getValue());
                Optional<WebEndpointMapping<HandlerMetadata<HandlerMethod, RequestMappingInfo>>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
            requestMappingInfoHandlerMethods.putAll(handlerMethodsMap);
        }
    }

    @Override
    protected void doStop() {

    }
}
