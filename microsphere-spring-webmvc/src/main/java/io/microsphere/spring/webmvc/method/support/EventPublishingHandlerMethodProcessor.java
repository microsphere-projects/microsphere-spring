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
package io.microsphere.spring.webmvc.method.support;

import io.microsphere.spring.context.OnceApplicationContextEventListener;
import io.microsphere.spring.util.BeanUtils;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadata;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataReadyEvent;
import io.microsphere.spring.webmvc.method.HandlerMethodArgumentResolvedEvent;
import io.microsphere.spring.webmvc.method.HandlerMethodArgumentsResolvedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.spring.webmvc.util.WebMvcUtils.getHandlerMethodArguments;

/**
 * {@link HandlerMethodArgumentResolver} and {@link HandlerMethodReturnValueHandler} processor of {@link HandlerMethod}
 * publishes the events:
 * <ul>
 *     <li>HandlerMethodArgumentResolvedEvent</li>
 *     <li>HandlerMethodArgumentsResolvedEvent</li>
 * </ul>
 * <p>
 * Besides, current class also optimize the performance using {@link HashMap}, instead of {@link ConcurrentMap}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodArgumentResolvedEvent
 * @see HandlerMethodArgumentsResolvedEvent
 * @see HandlerMethodArgumentResolver
 * @see HandlerMethodArgumentResolverComposite
 * @see HandlerMethodReturnValueHandler
 * @see HandlerMethodReturnValueHandlerComposite
 * @since 1.0.0
 */
public class EventPublishingHandlerMethodProcessor extends OnceApplicationContextEventListener<RequestMappingMetadataReadyEvent>
        implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingHandlerMethodProcessor.class);

    private final Map<MethodParameter, HandlerMethodArgumentResolver> argumentResolversCache = new HashMap<>(256);

    private final Map<MethodParameter, HandlerMethodReturnValueHandler> returnValueHandlersCache = new HashMap<>(256);

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return true;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HandlerMethodArgumentResolver resolver = argumentResolversCache.get(parameter);
        if (resolver == null) {
            return null;
        }
        Object argument = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        publishEvents(resolver, parameter, argument, webRequest);
        return argument;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {
        HandlerMethodReturnValueHandler handler = returnValueHandlersCache.get(returnType);
        if (handler == null) {
            return;
        }
        handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    private void publishEvents(HandlerMethodArgumentResolver resolver, MethodParameter parameter, Object argument, NativeWebRequest webRequest) {
        publishHandlerMethodArgumentResolvedEvent(resolver, parameter, argument, webRequest);
        publishHandlerMethodArgumentsResolvedEvent(resolver, parameter, argument, webRequest);
    }

    private void publishHandlerMethodArgumentResolvedEvent(HandlerMethodArgumentResolver resolver, MethodParameter parameter, Object argument, NativeWebRequest webRequest) {
        ApplicationEventPublisher publisher = getApplicationContext();
        publisher.publishEvent(new HandlerMethodArgumentResolvedEvent(resolver, parameter, argument, webRequest));
    }

    private void publishHandlerMethodArgumentsResolvedEvent(HandlerMethodArgumentResolver resolver, MethodParameter parameter, Object argument, NativeWebRequest webRequest) {
        int index = parameter.getParameterIndex();
        Object[] arguments = null;
        if (argument != null) {
            arguments = getHandlerMethodArguments(webRequest, parameter);
            if (index < arguments.length) {
                arguments[index] = argument;
            }
        }

        Method method = parameter.getMethod();
        int parameterCount = method.getParameterCount();

        if (index == parameterCount - 1) {
            if (arguments == null) {
                arguments = getHandlerMethodArguments(webRequest, parameter);
            }
            ApplicationEventPublisher publisher = getApplicationContext();
            publisher.publishEvent(new HandlerMethodArgumentsResolvedEvent(resolver, method, arguments, webRequest));
        }
    }

    @Override
    protected void onApplicationContextEvent(RequestMappingMetadataReadyEvent event) {
        initRequestMappingHandlerAdapters(event);
    }

    private void initRequestMappingHandlerAdapters(RequestMappingMetadataReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        List<RequestMappingMetadata> metadata = event.getMetadata();
        List<RequestMappingHandlerAdapter> adapters = BeanUtils.getSortedBeans(context, RequestMappingHandlerAdapter.class);
        for (int i = 0; i < adapters.size(); i++) {
            RequestMappingHandlerAdapter adapter = adapters.get(i);
            initRequestMappingHandlerAdapter(adapter, metadata);
        }
    }

    private void initRequestMappingHandlerAdapter(RequestMappingHandlerAdapter adapter, List<RequestMappingMetadata> metadata) {
        List<HandlerMethodArgumentResolver> resolvers = adapter.getArgumentResolvers();
        List<HandlerMethodReturnValueHandler> handlers = adapter.getReturnValueHandlers();
        for (RequestMappingMetadata requestMappingMetadata : metadata) {
            HandlerMethod handlerMethod = requestMappingMetadata.getHandlerMethod();
            Method method = handlerMethod.getMethod();
            int parameterCount = method.getParameterCount();
            for (int i = 0; i < parameterCount; i++) {
                MethodParameter methodParameter = new MethodParameter(method, i);
                initArgumentResolversCache(methodParameter, resolvers);
            }
            initReturnValueHandlers(new MethodParameter(method, -1), handlers);
        }
        adapter.setArgumentResolvers(Arrays.asList(this));
        adapter.setReturnValueHandlers(Arrays.asList(this));
    }

    private void initArgumentResolversCache(MethodParameter returnType, List<HandlerMethodArgumentResolver> resolvers) {
        int size = resolvers.size();
        for (int i = 0; i < size; i++) {
            HandlerMethodArgumentResolver resolver = resolvers.get(i);
            if (resolver.supportsParameter(returnType)) {
                // Put the first HandlerMethodArgumentResolver instance
                HandlerMethodArgumentResolver mappedResolver = argumentResolversCache.putIfAbsent(returnType, resolver);
                if (mappedResolver != null) {
                    logger.warn("The HandlerMethodArgumentResolver[class : '{}'] also supports the MethodParameter[{}] , the mapped one : {}",
                            getClassName(resolver), returnType, getClassName(mappedResolver));
                }
            }
        }
    }

    private void initReturnValueHandlers(MethodParameter methodParameter, List<HandlerMethodReturnValueHandler> handlers) {
        int size = handlers.size();
        for (int i = 0; i < size; i++) {
            HandlerMethodReturnValueHandler handler = handlers.get(i);
            if (handler.supportsReturnType(methodParameter)) {
                // Put the first HandlerMethodReturnValueHandler instance
                HandlerMethodReturnValueHandler mappedHandler = returnValueHandlersCache.putIfAbsent(methodParameter, handler);
                if (mappedHandler != null) {
                    logger.warn("The HandlerMethodReturnValueHandler[class : '{}'] also supports the MethodParameter[{}] , the mapped one : {}",
                            getClassName(handler), methodParameter, getClassName(mappedHandler));
                }
            }
        }
    }

    private String getClassName(Object instance) {
        return instance.getClass().getName();
    }
}
