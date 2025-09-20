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

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webmvc.metadata.HandlerMetadataWebEndpointMappingFactory;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataWebEndpointMappingFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Map;

import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.invoke.MethodHandleUtils.handleInvokeExactFailure;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.ID_HEADER_NAME;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.WEB_MVC;
import static io.microsphere.util.ArrayUtils.ofArray;

/**
 * The performance optimization {@link HandlerMapping} to process the forwarded request
 * from the reversed proxy web server, e.g, Spring Cloud Netflix Zuul, Spring Cloud Gateway or others.
 * The request must have a header named {@link WebEndpointMapping#ID_HEADER_NAME "microsphere_wem_id"},
 * which is a string presenting {@link WebEndpointMapping#getId() the id of endpoint}, used to
 * locate the actual {@link WebEndpointMapping#getEndpoint() endpoint} easily, such as {@link HandlerMethod},
 * {@link org.springframework.web.servlet.function.HandlerFunction} and {@link Controller}.
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

    private static final Logger logger = getLogger(ReversedProxyHandlerMapping.class);

    public static final int DEFAULT_ORDER = HIGHEST_PRECEDENCE + 1;

    /**
     * The method name of {@link AbstractHandlerMapping#getHandlerExecutionChain(Object, HttpServletRequest)}
     */
    private static final String getHandlerExecutionChainMethodName = "getHandlerExecutionChain";

    /**
     * The {@link MethodHandle} of {@link AbstractHandlerMapping#getHandlerExecutionChain(Object, HttpServletRequest)}
     */
    @Nullable
    private static final MethodHandle getHandlerExecutionChainMethodHandle;

    static {
        String methodName = getHandlerExecutionChainMethodName;
        Class<?>[] parameterTypes = ofArray(Object.class, HttpServletRequest.class);
        MethodHandle methodHandle = findVirtual(AbstractHandlerMapping.class, methodName, parameterTypes);
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
            logger.trace("No request header of the WebEndpointMapping ID [name : '{}'] is present", ID_HEADER_NAME);
            return null;
        }
        WebEndpointMapping webEndpointMapping = webEndpointMappingsCache.get(webEndpointMappingId);
        if (webEndpointMapping == null) {
            logger.trace("No WebEndpointMapping was found by the request header[name : '{}' , value : {}]", ID_HEADER_NAME, webEndpointMappingId);
            return null;
        }
        HandlerExecutionChain handlerExecutionChain = getHandlerExecutionChain(webEndpointMapping, request);
        return handlerExecutionChain;
    }

    @Nullable
    protected HandlerExecutionChain getHandlerExecutionChain(WebEndpointMapping webEndpointMapping, HttpServletRequest request) {
        HandlerExecutionChain handlerExecutionChain = null;
        if (isAbstractHandlerMapping(webEndpointMapping)) {
            AbstractHandlerMapping handlerMapping = (AbstractHandlerMapping) webEndpointMapping.getSource();
            Object handler = webEndpointMapping.getEndpoint();
            handlerExecutionChain = invokeGetHandlerExecutionChain(handlerMapping, handler, request);
        }
        return handlerExecutionChain;
    }

    /**
     * Invoke {@link AbstractHandlerMapping#getHandlerExecutionChain(Object, HttpServletRequest)}
     *
     * @param handlerMapping {@link RequestMappingHandlerMapping}
     * @param handler        {@link HandlerMethod}
     * @param request        {@link HttpServletRequest}
     * @return {@link HandlerExecutionChain} if invoke successfully, or <code>null</code>
     */
    HandlerExecutionChain invokeGetHandlerExecutionChain(AbstractHandlerMapping handlerMapping, Object handler, HttpServletRequest request) {
        HandlerExecutionChain handlerExecutionChain = null;
        MethodHandle methodHandle = getHandlerExecutionChainMethodHandle;
        if (methodHandle != null) {
            try {
                handlerExecutionChain = (HandlerExecutionChain) methodHandle.invokeExact(handlerMapping, handler, request);
            } catch (Throwable e) {
                handleInvokeExactFailure(e, methodHandle, handler, request);
            }
        }
        return handlerExecutionChain;
    }

    @Override
    public void onApplicationEvent(WebEndpointMappingsReadyEvent event) {
        Collection<WebEndpointMapping> webEndpointMappings = event.getMappings();
        int size = webEndpointMappings.size();
        Map<Integer, WebEndpointMapping> webEndpointMappingsMap = newFixedHashMap(size);
        event.getMappings().stream()
                .filter(this::isAbstractHandlerMapping)
                .forEach(mapping -> webEndpointMappingsMap.put(mapping.getId(), mapping));

        this.webEndpointMappingsCache = webEndpointMappingsMap;
    }

    boolean isAbstractHandlerMapping(WebEndpointMapping webEndpointMapping) {
        return WEB_MVC.equals(webEndpointMapping.getKind()) &&
                webEndpointMapping.getSource() instanceof AbstractHandlerMapping;
    }
}