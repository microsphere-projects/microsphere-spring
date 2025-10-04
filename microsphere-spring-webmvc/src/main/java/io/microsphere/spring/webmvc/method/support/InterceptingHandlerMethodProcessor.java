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

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.context.event.OnceApplicationContextEventListener;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.method.support.HandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webmvc.util.WebMvcUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodArguments;

/**
 * The {@link HandlerMethod} processor that callbacks {@link HandlerMethodAdvice} based on
 * {@link HandlerMethodArgumentResolver}, {@link HandlerMethodReturnValueHandler} and {@link HandlerInterceptor}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodAdvice
 * @see HandlerMethodInterceptor
 * @see HandlerMethodArgumentInterceptor
 * @see HandlerMethodArgumentResolver
 * @see HandlerMethodArgumentResolverComposite
 * @see HandlerMethodReturnValueHandler
 * @see HandlerMethodReturnValueHandlerComposite
 * @since 1.0.0
 */
public class InterceptingHandlerMethodProcessor extends OnceApplicationContextEventListener<WebEndpointMappingsReadyEvent>
        implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler, HandlerInterceptor {

    public static final String BEAN_NAME = "interceptingHandlerMethodProcessor";

    private static final Logger logger = getLogger(InterceptingHandlerMethodProcessor.class);

    private final Map<MethodParameter, MethodParameterContext> parameterContextsCache = new HashMap<>(256);

    private final Map<MethodParameter, ReturnTypeContext> returnTypeContextsCache = new HashMap<>(256);

    private List<HandlerMethodAdvice> handlerMethodAdvices;

    private List<HandlerMethodArgumentResolverAdvice> handlerMethodArgumentResolverAdvices;

    static class MethodParameterContext {

        private HandlerMethod method;

        private int parameterCount;

        private HandlerMethodArgumentResolver resolver;

        @Override
        public String toString() {
            return "MethodParameterContext{" +
                    "method=" + method +
                    ", parameterCount=" + parameterCount +
                    ", resolver=" + resolver +
                    '}';
        }
    }

    static class ReturnTypeContext {

        private HandlerMethod method;

        private HandlerMethodReturnValueHandler handler;

        @Override
        public String toString() {
            return "ReturnTypeContext{" +
                    "method=" + method +
                    ", handler=" + handler +
                    '}';
        }
    }

    @Override
    protected void onApplicationContextEvent(WebEndpointMappingsReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initHandlerMethodAdvices(context);
        initHandlerMethodArgumentResolverAdvices(context);
        initRequestMappingHandlerAdapters(event, context);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return this.parameterContextsCache.containsKey(parameter);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return this.returnTypeContextsCache.containsKey(returnType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        MethodParameterContext methodParameterContext = getParameterContext(parameter);

        HandlerMethodArgumentResolver resolver = methodParameterContext.resolver;

        beforeResolveArgument(parameter, methodParameterContext, mavContainer, webRequest, binderFactory);

        Object argument = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        afterResolveArgument(parameter, argument, methodParameterContext, mavContainer, webRequest, binderFactory);

        beforeExecute(parameter, methodParameterContext, webRequest, argument);

        return argument;
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {
        ReturnTypeContext context = getReturnTypeContext(returnType);

        HandlerMethodReturnValueHandler handler = context.handler;

        HandlerMethod handlerMethod = context.method;

        afterExecute(webRequest, handlerMethod, context);

        handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception error) throws Exception {
        if (error != null) {
            afterExecute(request, handler, error);
        }
    }

    private void initHandlerMethodArgumentResolverAdvices(ApplicationContext context) {
        List<HandlerMethodArgumentResolverAdvice> advices = getSortedBeans(context, HandlerMethodArgumentResolverAdvice.class);
        this.handlerMethodArgumentResolverAdvices = advices;
    }

    private void initHandlerMethodAdvices(ApplicationContext context) {
        this.handlerMethodAdvices = getSortedBeans(context, HandlerMethodAdvice.class);
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
        List<HandlerMethodArgumentResolver> resolvers = adapter.getArgumentResolvers();
        List<HandlerMethodReturnValueHandler> handlers = adapter.getReturnValueHandlers();
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

        List<HandlerMethodArgumentResolver> newResolvers = new ArrayList<>(resolvers.size() + 1);
        // Current instance is the first element, others as the fallback if first can't resolve
        newResolvers.add(this);
        newResolvers.addAll(resolvers);

        List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>(handlers.size() + 1);
        // Current instance is the first element, others as the fallback if first can't handle
        newHandlers.add(this);
        newHandlers.addAll(handlers);

        adapter.setArgumentResolvers(newResolvers);
        adapter.setReturnValueHandlers(newHandlers);
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

    private void initReturnTypeContextsCache(HandlerMethod handlerMethod, List<HandlerMethodReturnValueHandler> handlers) {
        HandlerMethodReturnValueHandler handler = resolveReturnValueHandler(handlerMethod, handlers);
        if (handler != null) {
            ReturnTypeContext context = new ReturnTypeContext();
            context.method = handlerMethod;
            context.handler = handler;

            MethodParameter returnType = handlerMethod.getReturnType();
            returnTypeContextsCache.put(returnType, context);
        }
    }

    private MethodParameterContext getParameterContext(MethodParameter parameter) {
        MethodParameterContext context = parameterContextsCache.get(parameter);
        logger.trace("The MethodParameterContext is gotten by the parameter[{}] : {}", parameter, context);
        return context;
    }

    private ReturnTypeContext getReturnTypeContext(MethodParameter returnType) {
        ReturnTypeContext context = returnTypeContextsCache.get(returnType);
        logger.trace("The ReturnTypeContext is gotten by the return type[{}] : {}", returnType, context);
        return context;
    }

    private HandlerMethod resolveHandlerMethod(Object handler) {
        return handler instanceof HandlerMethod ? (HandlerMethod) handler : null;
    }

    private void beforeResolveArgument(MethodParameter parameter, MethodParameterContext methodParameterContext,
                                       ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                       WebDataBinderFactory binderFactory) throws Exception {
        for (int i = 0; i < handlerMethodArgumentResolverAdvices.size(); i++) {
            HandlerMethodArgumentResolverAdvice advice = handlerMethodArgumentResolverAdvices.get(i);
            advice.beforeResolveArgument(parameter, mavContainer, webRequest, binderFactory);
        }

        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice advice = handlerMethodAdvices.get(i);
            advice.beforeResolveArgument(parameter, methodParameterContext.method, webRequest);
        }
    }

    private void afterResolveArgument(MethodParameter parameter, Object argument, MethodParameterContext methodParameterContext,
                                      ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) throws Exception {
        for (int i = 0; i < handlerMethodArgumentResolverAdvices.size(); i++) {
            HandlerMethodArgumentResolverAdvice advice = handlerMethodArgumentResolverAdvices.get(i);
            advice.afterResolveArgument(parameter, argument, mavContainer, webRequest, binderFactory);
        }

        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice advice = handlerMethodAdvices.get(i);
            advice.afterResolveArgument(parameter, argument, methodParameterContext.method, webRequest);
        }
    }

    private void beforeExecute(MethodParameter parameter, MethodParameterContext methodParameterContext,
                               NativeWebRequest webRequest, Object argument) throws Exception {
        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice advice = handlerMethodAdvices.get(i);
            int parameterCount = methodParameterContext.parameterCount;
            Object[] arguments = resolveArguments(webRequest, parameter, argument);

            int parameterIndex = parameter.getParameterIndex();
            if (parameterIndex == parameterCount - 1) {
                HandlerMethod handlerMethod = methodParameterContext.method;
                advice.beforeExecuteMethod(handlerMethod, arguments, webRequest);
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

    void afterExecute(HttpServletRequest request, Object handler, @Nullable Exception error) throws Exception {
        HandlerMethod handlerMethod = resolveHandlerMethod(handler);
        if (handlerMethod != null) {
            ServletWebRequest webRequest = new ServletWebRequest(request);
            Object[] arguments = getArguments(request, handlerMethod);
            afterExecute(webRequest, handlerMethod, arguments, null, error);
        }
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, Object[] arguments,
                              @Nullable Object returnValue, @Nullable Exception error) throws Exception {
        for (int i = 0; i < handlerMethodAdvices.size(); i++) {
            HandlerMethodAdvice advice = handlerMethodAdvices.get(i);
            advice.afterExecuteMethod(handlerMethod, arguments, returnValue, error, webRequest);
        }
    }

    Object[] getArguments(NativeWebRequest webRequest, HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(webRequest, handlerMethod);
    }

    Object[] getArguments(HttpServletRequest request, HandlerMethod handlerMethod) {
        return WebMvcUtils.getHandlerMethodArguments(request, handlerMethod);
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

    HandlerMethodReturnValueHandler resolveReturnValueHandler(HandlerMethod handlerMethod, List<HandlerMethodReturnValueHandler> handlers) {
        MethodParameter returnType = handlerMethod.getReturnType();
        int size = handlers.size();
        HandlerMethodReturnValueHandler targetHandler = null;
        for (int i = 0; i < size; i++) {
            HandlerMethodReturnValueHandler handler = handlers.get(i);
            if (handler.supportsReturnType(returnType)) {
                // Put the first HandlerMethodReturnValueHandler instance
                if (targetHandler == null) {
                    targetHandler = handler;
                } else {
                    logger.warn("The HandlerMethodReturnValueHandler[class : '{}'] also supports the return type[{}] , the mapped one : {}",
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