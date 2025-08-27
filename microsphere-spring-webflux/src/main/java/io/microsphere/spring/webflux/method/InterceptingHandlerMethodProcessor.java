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
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodArguments;
import static java.util.Collections.emptyList;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.error;

/**
 * The {@link HandlerMethod} processor that callbacks {@link HandlerMethodAdvice} based on
 * {@link HandlerMethodArgumentResolver}, {@link HandlerMethodReturnValueHandler}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodAdvice
 * @see HandlerMethodInterceptor
 * @see HandlerMethodArgumentInterceptor
 * @see HandlerMethodArgumentResolver
 * @see HandlerMethodReturnValueHandler
 * @see HandlerMethodReturnValueHandlerComposite
 * @since 1.0.0
 */
public class InterceptingHandlerMethodProcessor extends OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>
        implements HandlerMethodArgumentResolver, HandlerResultHandler {

    public static final String BEAN_NAME = "interceptingHandlerMethodProcessor";

    private static final Logger logger = getLogger(InterceptingHandlerMethodProcessor.class);

    private final Map<MethodParameter, MethodParameterContext> parameterContextsCache = new HashMap<>(256);

    private final Map<MethodParameter, ReturnTypeContext> returnTypeContextsCache = new HashMap<>(256);

    private List<HandlerMethodAdvice> handlerMethodAdvices = emptyList();

    private List<HandlerResultHandler> handlerResultHandlers = emptyList();

    static class MethodParameterContext {

        private HandlerMethod method;

        private int parameterCount;

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
        return this.parameterContextsCache.containsKey(parameter);
    }

    @Override
    public boolean supports(HandlerResult result) {
        for (int i = 1; i < handlerResultHandlers.size(); i++) {
            HandlerResultHandler handler = handlerResultHandlers.get(i);
            if (handler.supports(result)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        MethodParameterContext methodParameterContext = getParameterContext(parameter);
        if (methodParameterContext == null) {
            logger.trace("The MethodParameterContext can't be found by the MethodParameter[{}]", parameter);
            return null;
        }

        HandlerMethodArgumentResolver resolver = methodParameterContext.resolver;

        ServerWebRequest webRequest = new ServerWebRequest(exchange);

        Mono<Object> result;

        try {
            beforeResolveArgument(parameter, webRequest, methodParameterContext);

            result = resolver.resolveArgument(parameter, bindingContext, exchange);

            Object argument = result.toFuture().get();

            afterResolveArgument(parameter, argument, webRequest, methodParameterContext);

            beforeExecute(parameter, methodParameterContext, webRequest, argument);

        } catch (Exception e) {
            result = error(e);
        }

        return result;
    }


    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult handlerResult) {
        ReturnTypeContext context = getReturnTypeContext(handlerResult);
        if (context == null) {
            logger.trace("The ReturnTypeContext can't be found by the return type[{}]", handlerResult.getReturnTypeSource());
            return empty();
        }

        NativeWebRequest webRequest = new ServerWebRequest(exchange);

        HandlerResultHandler handler = context.handler;

        HandlerMethod handlerMethod = context.method;

        Mono<Void> result;
        try {
            result = handler.handleResult(exchange, handlerResult);
            afterExecute(webRequest, handlerMethod, context);
        } catch (Exception e) {
            result = error(e);
        }
        return result;
    }

    private void initHandlerMethodAdvices(ApplicationContext context) {
        this.handlerMethodAdvices = getSortedBeans(context, HandlerMethodAdvice.class);
    }

    private void initHandlerResultHandlers(ApplicationContext context) {
        this.handlerResultHandlers = getHandlerResultHandlers(context);
        this.handlerResultHandlers.add(0, this);
    }

    private void initRequestMappingHandlerAdapters(WebEndpointMappingsReadyEvent event, ApplicationContext context) {
        Collection<WebEndpointMapping> mappings = event.getMappings();
        List<RequestMappingHandlerAdapter> adapters = getSortedBeans(context, RequestMappingHandlerAdapter.class);
        List<HandlerResultHandler> handlers = this.handlerResultHandlers;
        for (int i = 0; i < adapters.size(); i++) {
            RequestMappingHandlerAdapter adapter = adapters.get(i);
            initRequestMappingHandlerAdapter(adapter, handlers, mappings);
        }
    }

    private void initRequestMappingHandlerAdapter(RequestMappingHandlerAdapter adapter, List<HandlerResultHandler> handlers,
                                                  Collection<WebEndpointMapping> webEndpointMappings) {
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
                    initMethodParameterContextsCache(methodParameter, handlerMethod, parameterCount, resolvers);
                }
                initReturnTypeContextsCache(handlerMethod, handlers);
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
                                                  int parameterCount, List<HandlerMethodArgumentResolver> resolvers) {
        HandlerMethodArgumentResolver resolver = resolveArgumentResolver(methodParameter, resolvers);
        if (resolver != null) {
            MethodParameterContext context = new MethodParameterContext();
            context.method = handlerMethod;
            context.parameterCount = parameterCount;
            context.resolver = resolver;
            parameterContextsCache.put(methodParameter, context);
        }
    }

    private void initReturnTypeContextsCache(HandlerMethod handlerMethod, List<HandlerResultHandler> handlers) {
        HandlerResultHandler handler = resolveHandlerResultHandler(handlerMethod, handlers);
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

    private ReturnTypeContext getReturnTypeContext(HandlerResult result) {
        MethodParameter returnType = result.getReturnTypeSource();
        return returnTypeContextsCache.get(returnType);
    }

    private HandlerMethod resolveHandlerMethod(Object handler) {
        return handler instanceof HandlerMethod ? (HandlerMethod) handler : null;
    }

    private void beforeResolveArgument(MethodParameter parameter, NativeWebRequest webRequest, MethodParameterContext methodParameterContext) throws Exception {
        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
            HandlerMethod handlerMethod = methodParameterContext.method;
            handlerMethodAdvice.beforeResolveArgument(parameter, handlerMethod, webRequest);
        }
    }

    private void afterResolveArgument(MethodParameter parameter, Object argument, NativeWebRequest webRequest, MethodParameterContext methodParameterContext) throws Exception {
        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
            HandlerMethod handlerMethod = methodParameterContext.method;
            handlerMethodAdvice.afterResolveArgument(parameter, argument, handlerMethod, webRequest);
        }
    }


    private void beforeExecute(MethodParameter parameter, MethodParameterContext methodParameterContext,
                               NativeWebRequest webRequest, Object argument) throws Exception {
        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
            int parameterCount = methodParameterContext.parameterCount;
            Object[] arguments = resolveArguments(parameter, argument, webRequest);

            int parameterIndex = parameter.getParameterIndex();
            if (parameterIndex == parameterCount - 1) {
                HandlerMethod handlerMethod = methodParameterContext.method;
                handlerMethodAdvice.beforeExecuteMethod(handlerMethod, arguments, webRequest);
            }
        }
    }

    private void afterExecute(NativeWebRequest webRequest, @Nullable Object returnValue, ReturnTypeContext context) throws Exception {
        HandlerMethod handlerMethod = context.method;
        Object[] arguments = getArguments(webRequest, handlerMethod);
        afterExecute(webRequest, handlerMethod, arguments, returnValue);
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, Object[] args,
                              @Nullable Object returnValue) throws Exception {
        afterExecute(webRequest, handlerMethod, args, returnValue, null);
    }

    private void afterExecute(NativeWebRequest webRequest, Object handler, @Nullable Exception error) throws Exception {
        HandlerMethod handlerMethod = resolveHandlerMethod(handler);
        if (handlerMethod != null) {
            Object[] arguments = getArguments(webRequest, handlerMethod);
            afterExecute(webRequest, handlerMethod, arguments, null, error);
        }
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, Object[] arguments,
                              @Nullable Object returnValue, @Nullable Exception error) throws Exception {
        for (int i = 0; i < this.handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice handlerMethodAdvice = this.handlerMethodAdvices.get(i);
            handlerMethodAdvice.afterExecuteMethod(handlerMethod, arguments, returnValue, error, webRequest);
        }
    }

    private Object[] getArguments(NativeWebRequest webRequest, HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(webRequest, handlerMethod);
    }

    private Object[] resolveArguments(MethodParameter parameter, Object argument, NativeWebRequest webRequest) {
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

    private HandlerMethodArgumentResolver resolveArgumentResolver(MethodParameter methodParameter, List<HandlerMethodArgumentResolver> resolvers) {
        int size = resolvers.size();
        HandlerMethodArgumentResolver targetResolver = null;
        for (int i = 0; i < size; i++) {
            HandlerMethodArgumentResolver resolver = resolvers.get(i);
            if (resolver.supportsParameter(methodParameter)) {
                // Put the first HandlerMethodArgumentResolver instance
                if (targetResolver == null) {
                    targetResolver = resolver;
                } else {
                    logger.warn("The HandlerMethodArgumentResolver[class : '{}'] also supports the MethodParameter[{}] , the mapped one : {}",
                            getClassName(resolver), methodParameter, getClassName(targetResolver));
                }
            }
        }
        return targetResolver;
    }

    private HandlerResultHandler resolveHandlerResultHandler(HandlerMethod handlerMethod, List<HandlerResultHandler> handlers) {
        MethodParameter returnType = handlerMethod.getReturnType();
        HandlerResult handlerResult = new HandlerResult(handlerMethod, null, returnType);
        int size = handlers.size();
        HandlerResultHandler targetHandler = null;
        for (int i = 1; i < size; i++) {
            HandlerResultHandler handler = handlers.get(i);
            if (handler.supports(handlerResult)) {
                // Put the first HandlerMethodReturnValueHandler instance
                if (targetHandler == null) {
                    targetHandler = handler;
                    break;
                } else {
                    logger.warn("The HandlerResultHandler[class : '{}'] also supports the return type[{}] , the mapped one : {}",
                            getClassName(handler), handlerMethod, getClassName(targetHandler));
                }
            }
        }
        return targetHandler;
    }

    private String getClassName(Object instance) {
        return instance.getClass().getName();
    }
}
