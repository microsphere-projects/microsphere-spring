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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.ID_HEADER_NAME;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;
import static java.lang.invoke.MethodHandles.Lookup.PROTECTED;
import static java.lang.invoke.MethodType.methodType;

/**
 * The Spring WebMVC {@link RequestMappingHandlerMapping} for Reversed Proxy, e.g,
 * Spring Cloud Netflix Zuul, Spring Cloud Gateway , other reversed proxy web server.
 * The HTTP request must be forwarded by the reversed proxy web server, which contains
 * the specified header named {@link WebEndpointMapping#ID_HEADER_NAME}, {@link ReversedProxyRequestMappingHandlerMapping}
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
public class ReversedProxyRequestMappingHandlerMapping extends AbstractHandlerMapping implements ApplicationListener<WebEndpointMappingsReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ReversedProxyRequestMappingHandlerMapping.class);

    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;

    private static final String getHandlerExecutionChainMethodName = "getHandlerExecutionChain";

    private static final MethodHandle getHandlerExecutionChainMethodHandle;


    static {
        Class<?> declaredClass = AbstractHandlerMapping.class;
        String methodName = getHandlerExecutionChainMethodName;
        MethodType methodType = methodType(HandlerExecutionChain.class, Object.class, HttpServletRequest.class);
        MethodHandle methodHandle = null;
        try {
            MethodHandles.Lookup lookup = createLookup();
            methodHandle = lookup.findVirtual(declaredClass, methodName, methodType);
        } catch (Throwable e) {
            logger.error("The method {}{} can't be found in the {}", methodName, methodType, declaredClass.getName(), e);
        }
        getHandlerExecutionChainMethodHandle = methodHandle;
    }

    private static MethodHandles.Lookup createLookup() throws Throwable {
        MethodHandles.Lookup lookup = null;
        Class<MethodHandles.Lookup> lookupClass = MethodHandles.Lookup.class;
        Constructor<?>[] constructors = lookupClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 2) {
                constructor.setAccessible(true);
                lookup = (MethodHandles.Lookup) constructor.newInstance(RequestMappingHandlerMapping.class, PROTECTED);
                constructor.setAccessible(false);
                break;
            }
        }
        return lookup;
    }


    private Map<Integer, WebEndpointMapping> webEndpointMappingsCache;

    public ReversedProxyRequestMappingHandlerMapping() {
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
        HandlerExecutionChain handlerExecutionChain = getHandlerExecutionChain(webEndpointMapping, request);
        return handlerExecutionChain;
    }

    @Nullable
    protected HandlerExecutionChain getHandlerExecutionChain(WebEndpointMapping
                                                                     webEndpointMapping, HttpServletRequest request) {
        HandlerExecutionChain handlerExecutionChain = null;
        Object endpoint = webEndpointMapping.getEndpoint();
        if (endpoint instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) endpoint;
            Object source = webEndpointMapping.getSource();
            if (source instanceof RequestMappingHandlerMapping) {
                RequestMappingHandlerMapping handlerMapping = (RequestMappingHandlerMapping) source;
                Object handler = handlerMethod.createWithResolvedBean();
                MethodHandle methodHandle = getHandlerExecutionChainMethodHandle;
                if (methodHandle != null) {
                    try {
                        handlerExecutionChain = (HandlerExecutionChain) methodHandle.invoke(handlerMapping, handler, request);
                    } catch (Throwable e) {
                        logger.error("The method {}{} can't be executed in the {}",
                                getHandlerExecutionChainMethodName,
                                methodHandle.type(),
                                handlerMapping.getClass().getName());
                    }
                }
            }
        }
        return handlerExecutionChain;
    }

    @Override
    public void onApplicationEvent(WebEndpointMappingsReadyEvent event) {
        Map<Integer, WebEndpointMapping> webEndpointMappingsMap = new HashMap<>();
        event.getMappings().stream()
                .filter(this::isRequestMappingHandlerMapping)
                .forEach(webEndpointMapping -> {
                    webEndpointMappingsMap.put(webEndpointMapping.getId(), webEndpointMapping);
                });

        this.webEndpointMappingsCache = webEndpointMappingsMap;
    }

    private boolean isRequestMappingHandlerMapping(WebEndpointMapping webEndpointMapping) {
        return WEB_MVC.equals(webEndpointMapping.getKind()) &&
                webEndpointMapping.getSource() instanceof RequestMappingHandlerMapping;
    }

}
