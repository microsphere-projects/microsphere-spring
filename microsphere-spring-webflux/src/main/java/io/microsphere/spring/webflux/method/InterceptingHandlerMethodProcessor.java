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
package io.microsphere.spring.webflux.method;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.context.event.OnceApplicationContextEventListener;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.method.support.HandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webflux.context.request.ServerWebRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.web.util.MonoUtils.getValue;
import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodArguments;
import static java.util.Collections.emptyList;
import static org.springframework.web.reactive.HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

/**
 * The {@link HandlerMethod} processor that callbacks {@link HandlerMethodAdvice} beans based on
 * {@link HandlerMethodArgumentResolver}, {@link HandlerResultHandler}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodAdvice
 * @see HandlerMethodInterceptor
 * @see HandlerMethodArgumentInterceptor
 * @see HandlerMethodArgumentResolver
 * @see HandlerResultHandler
 * @see WebEndpointMappingsReadyEvent
 * @since 1.0.0
 */
public class InterceptingHandlerMethodProcessor extends OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>
        implements HandlerMethodArgumentResolver, HandlerResultHandler, WebExceptionHandler {

    public static final String BEAN_NAME = "interceptingHandlerMethodProcessor";

    private static final Logger logger = getLogger(InterceptingHandlerMethodProcessor.class);

    private final Map<MethodParameter, MethodParameterContext> parameterContextsCache = new HashMap<>(256);

    private final Map<MethodParameter, ReturnTypeContext> returnTypeContextsCache = new HashMap<>(256);

    private List<HandlerMethodAdvice> handlerMethodAdvices = emptyList();

    private List<HandlerResultHandler> handlerResultHandlers = emptyList();

    static class MethodParameterContext {

        private HandlerMethod method;

        private HandlerMethodArgumentResolver resolver;

    }

    static class ReturnTypeContext {

        private HandlerMethod method;

        private HandlerResultHandler handler;

    }

    @Override
    protected void onApplicationContextEvent(WebEndpointMappingsReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initHandlerMethodAdvices(context);
        initHandlerResultHandlers(context);
        initRequestMappingHandlerAdapters(event, context);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return getParameterContext(parameter) != null;
    }

    @Override
    public boolean supports(HandlerResult result) {
        return getReturnTypeContext(result) != null;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        MethodParameterContext context = getParameterContext(parameter);

        HandlerMethodArgumentResolver resolver = context.resolver;

        ServerWebRequest webRequest = new ServerWebRequest(exchange);

        Mono<Object> result;

        HandlerMethod handlerMethod = context.method;

        try {
            beforeResolveArgument(parameter, webRequest, handlerMethod);

            result = resolver.resolveArgument(parameter, bindingContext, exchange);

            Object argument = getValue(result);

            Object[] arguments = resolveArguments(webRequest, parameter, argument);

            afterResolveArgument(parameter, argument, webRequest, handlerMethod);

            beforeExecute(parameter, webRequest, handlerMethod, arguments);

            // rebuild the result
            result = just(argument);
        } catch (Exception e) {
            result = error(e);
        }
        return result;
    }


    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult handlerResult) {
        ReturnTypeContext context = getReturnTypeContext(handlerResult);

        NativeWebRequest webRequest = new ServerWebRequest(exchange);

        HandlerResultHandler handler = context.handler;

        HandlerMethod handlerMethod = context.method;

        Mono<Void> result = handler.handleResult(exchange, handlerResult);
        try {
            afterExecute(webRequest, handlerMethod, handlerResult.getReturnValue());
        } catch (Throwable e) {
            result = error(e);
        }
        return result;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        NativeWebRequest webRequest = new ServerWebRequest(exchange);
        HandlerMethod handlerMethod = getHandlerMethod(exchange);
        Mono<Void> result;
        try {
            afterExecute(webRequest, handlerMethod, ex);
            result = error(ex);
        } catch (Throwable e) {
            result = error(e);
        }
        return result;
    }

    private HandlerMethod getHandlerMethod(ServerWebExchange exchange) {
        return exchange.getAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE);
    }

    private void initHandlerMethodAdvices(ApplicationContext context) {
        this.handlerMethodAdvices = getSortedBeans(context, HandlerMethodAdvice.class);
    }

    private void initHandlerResultHandlers(ApplicationContext context) {
        this.handlerResultHandlers = getHandlerResultHandlers(context);
        this.handlerResultHandlers.remove(this);
        this.handlerResultHandlers.add(0, this);
    }

    private void initRequestMappingHandlerAdapters(WebEndpointMappingsReadyEvent event, ApplicationContext context) {
        Collection<WebEndpointMapping> mappings = event.getMappings();
        List<RequestMappingHandlerAdapter> adapters = getSortedBeans(context, RequestMappingHandlerAdapter.class);
        for (int i = 0; i < adapters.size(); i++) {
            RequestMappingHandlerAdapter adapter = adapters.get(i);
            initRequestMappingHandlerAdapter(adapter, mappings);
        }
    }

    private void initRequestMappingHandlerAdapter(RequestMappingHandlerAdapter adapter, Collection<WebEndpointMapping> webEndpointMappings) {
        List<HandlerMethodArgumentResolver> resolvers = getHandlerMethodArgumentResolvers(adapter);
        for (WebEndpointMapping webEndpointMapping : webEndpointMappings) {
            Object endpoint = webEndpointMapping.getEndpoint();
            if (endpoint instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) endpoint;
                MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
                Method method = handlerMethod.getMethod();
                int parameterCount = method.getParameterCount();
                for (int i = 0; i < parameterCount; i++) {
                    MethodParameter methodParameter = methodParameters[i];
                    initMethodParameterContextsCache(methodParameter, handlerMethod, resolvers);
                }
                initReturnTypeContextsCache(handlerMethod);
            }
        }
        resolvers.add(0, this);
    }

    private List<HandlerResultHandler> getHandlerResultHandlers(ApplicationContext context) {
        DispatcherHandler dispatcherHandler = context.getBean(DispatcherHandler.class);
        return getFieldValue(dispatcherHandler, "resultHandlers");
    }

    private List<HandlerMethodArgumentResolver> getHandlerMethodArgumentResolvers(RequestMappingHandlerAdapter adapter) {
        Object methodResolver = getFieldValue(adapter, "methodResolver");
        return getFieldValue(methodResolver, "requestMappingResolvers");
    }

    private void initMethodParameterContextsCache(MethodParameter methodParameter, HandlerMethod handlerMethod,
                                                  List<HandlerMethodArgumentResolver> resolvers) {
        HandlerMethodArgumentResolver resolver = resolveArgumentResolver(methodParameter, resolvers);
        if (resolver != null) {
            MethodParameterContext context = new MethodParameterContext();
            context.method = handlerMethod;
            context.resolver = resolver;
            parameterContextsCache.put(methodParameter, context);
        }
    }

    private void initReturnTypeContextsCache(HandlerMethod handlerMethod) {
        HandlerResult handlerResult = new HandlerResult(handlerMethod, null, handlerMethod.getReturnType());
        HandlerResultHandler handler = resolveHandlerResultHandler(handlerResult);
        if (handler != null) {
            ReturnTypeContext context = new ReturnTypeContext();
            context.method = handlerMethod;
            context.handler = handler;

            MethodParameter returnType = handlerMethod.getReturnType();
            returnTypeContextsCache.put(returnType, context);
        }
    }

    private MethodParameterContext getParameterContext(MethodParameter parameter) {
        return parameterContextsCache.get(parameter);
    }

    ReturnTypeContext getReturnTypeContext(HandlerResult handlerResult) {
        MethodParameter returnType = handlerResult.getReturnTypeSource();
        ReturnTypeContext returnTypeContext = returnTypeContextsCache.get(returnType);
        if (returnTypeContext == null) {
            logger.trace("No ReturnTypeContext was found by the return type[{}]", returnType);
            HandlerResultHandler handler = resolveHandlerResultHandler(handlerResult);
            if (handler != null) {
                returnTypeContext = new ReturnTypeContext();
                returnTypeContext.handler = handler;
                returnTypeContext.method = resolveHandlerMethod(handlerResult.getHandler());
            }
        }
        return returnTypeContext;
    }

    private HandlerMethod resolveHandlerMethod(Object handler) {
        return handler instanceof HandlerMethod ? (HandlerMethod) handler : null;
    }

    private void beforeResolveArgument(MethodParameter parameter, NativeWebRequest webRequest, HandlerMethod handlerMethod) throws Exception {
        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
            handlerMethodAdvice.beforeResolveArgument(parameter, handlerMethod, webRequest);
        }
    }

    private void afterResolveArgument(MethodParameter parameter, Object argument, NativeWebRequest webRequest, HandlerMethod handlerMethod) throws Exception {
        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
            handlerMethodAdvice.afterResolveArgument(parameter, argument, handlerMethod, webRequest);
        }
    }

    private void beforeExecute(MethodParameter parameter, NativeWebRequest webRequest, HandlerMethod handlerMethod,
                               Object[] arguments) throws Exception {
        int parameterIndex = parameter.getParameterIndex();
        if (parameterIndex == arguments.length - 1) { // match last parameter
            for (int i = 0; i < handlerMethodAdvices.size(); i++) {
                HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
                handlerMethodAdvice.beforeExecuteMethod(handlerMethod, arguments, webRequest);
            }
        }
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, @Nullable Object returnValue) throws Exception {
        afterExecute(webRequest, handlerMethod, returnValue, null);
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, Throwable error) throws Exception {
        afterExecute(webRequest, handlerMethod, null, error);
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, @Nullable Object returnValue,
                              @Nullable Throwable error) throws Exception {
        Object[] arguments = getArguments(webRequest, handlerMethod);
        for (int i = 0; i < this.handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
            handlerMethodAdvice.afterExecuteMethod(handlerMethod, arguments, returnValue, error, webRequest);
        }
    }

    private Object[] getArguments(NativeWebRequest webRequest, HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(webRequest, handlerMethod);
    }

    Object[] resolveArguments(NativeWebRequest webRequest, MethodParameter parameter, Object argument) {
        int index = parameter.getParameterIndex();
        Object[] arguments = null;
        if (argument != null) {
            arguments = getHandlerMethodArguments(webRequest, parameter);
            if (index < arguments.length) {
                arguments[index] = argument;
            }
        }
        return arguments;
    }

    HandlerMethodArgumentResolver resolveArgumentResolver(MethodParameter methodParameter, List<HandlerMethodArgumentResolver> resolvers) {
        int size = resolvers.size();
        HandlerMethodArgumentResolver targetResolver = null;
        for (int i = 1; i < size; i++) {
            HandlerMethodArgumentResolver resolver = resolvers.get(i);
            if (resolver.supportsParameter(methodParameter)) {
                // Put the first HandlerMethodArgumentResolver instance
                targetResolver = resolver;
                break;
            }
        }

        if (targetResolver == null) {
            logger.warn("No HandlerMethodArgumentResolver was found to support the parameter[{}]", methodParameter);
        }
        return targetResolver;
    }

    HandlerResultHandler resolveHandlerResultHandler(HandlerResult handlerResult) {
        List<HandlerResultHandler> handlers = this.handlerResultHandlers;
        int size = handlers.size();
        HandlerResultHandler targetHandler = null;
        for (int i = 1; i < size; i++) {
            HandlerResultHandler handler = handlers.get(i);
            if (handler.supports(handlerResult)) {
                // Put the first HandlerResultHandler instance
                targetHandler = handler;
                break;
            }
        }

        if (targetHandler == null) {
            logger.warn("No HandlerResultHandler was found to support the return type[{}]", handlerResult.getReturnType());
        }

        return targetHandler;
    }
}
