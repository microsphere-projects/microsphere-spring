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
import io.microsphere.spring.web.metadata.WebEndpointMappingsReadyEvent;
import io.microsphere.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.ID_HEADER_NAME;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;

/**
 * The performance optimization {@link HandlerMapping} to process the forwarded request
 * from the reversed proxy web server, e.g, Spring Cloud Netflix Zuul, Spring Cloud Gateway or others.
 * The request must have a header named {@link WebEndpointMapping#ID_HEADER_NAME "microsphere_wem_id"},
 * which is a string presenting {@link WebEndpointMapping#getId() the id of endpoint}, used to
 * locate the actual {@link WebEndpointMapping#getEndpoint() endpoint} easily, such as {@link HandlerMethod},
 * {@link HandlerFunction} and {@link Controller}.
 * <p>
 * As a result, {@link ReversedProxyHandlerMapping} has the higher precedence than others, which ensures that it
 * prioritizes {@link HandlerMapping#getHandler(HttpServletRequest) getting the handler} and avoid the duplication
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

    private static final Logger logger = LoggerFactory.getLogger(ReversedProxyHandlerMapping.class);

    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;

    private static final String getHandlerExecutionChainMethodName = "getHandlerExecutionChain";

    private static final MethodHandle getHandlerExecutionChainMethodHandle;

    static {
        Class<?> declaredClass = AbstractHandlerMapping.class;
        String methodName = getHandlerExecutionChainMethodName;
        MethodHandle methodHandle = null;
        Class<?>[] parameterTypes = ArrayUtils.of(Object.class, HttpServletRequest.class);
        try {
            methodHandle = findVirtual(RequestMappingHandlerMapping.class, methodName, parameterTypes);
        } catch (Throwable e) {
            logger.error("The method {}{} can't be found in the {}", methodName, Arrays.toString(parameterTypes), declaredClass.getName(), e);
        }
        getHandlerExecutionChainMethodHandle = methodHandle;
    }

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
        HandlerExecutionChain handlerExecutionChain = getHandlerExecutionChain(webEndpointMapping, request);
        return handlerExecutionChain;
    }

    @Nullable
    protected HandlerExecutionChain getHandlerExecutionChain(WebEndpointMapping
                                                                     webEndpointMapping, HttpServletRequest request) {
        HandlerExecutionChain handlerExecutionChain = null;
        Object source = webEndpointMapping.getSource();
        if (source instanceof RequestMappingHandlerMapping) {
            HandlerMethod handlerMethod = (HandlerMethod) webEndpointMapping.getEndpoint();
            RequestMappingHandlerMapping handlerMapping = (RequestMappingHandlerMapping) source;
            Object handler = handlerMethod;
            MethodHandle methodHandle = getHandlerExecutionChainMethodHandle;
            if (methodHandle != null) {
                try {
                    handlerExecutionChain = (HandlerExecutionChain) methodHandle.invokeExact(handlerMapping, handler, request);
                } catch (Throwable e) {
                    logger.error("The method {}{} can't be executed in the {}",
                            getHandlerExecutionChainMethodName,
                            methodHandle.type(),
                            handlerMapping.getClass().getName());
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
