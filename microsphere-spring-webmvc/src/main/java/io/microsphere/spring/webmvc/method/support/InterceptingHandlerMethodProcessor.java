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
import io.microsphere.spring.web.method.support.HandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadata;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.spring.util.BeanUtils.getOptionalBean;
import static io.microsphere.spring.util.BeanUtils.getSortedBeans;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.getHandlerMethodArguments;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * The {@link HandlerMethod} processor based on
 * {@link HandlerMethodArgumentResolver}, {@link HandlerMethodReturnValueHandler} and {@link HandlerInterceptor}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodInterceptor
 * @see HandlerMethodArgumentInterceptor
 * @see HandlerMethodAdvice
 * @see HandlerMethodArgumentResolver
 * @see HandlerMethodArgumentResolverComposite
 * @see HandlerMethodReturnValueHandler
 * @see HandlerMethodReturnValueHandlerComposite
 * @since 1.0.0
 */
public class InterceptingHandlerMethodProcessor extends OnceApplicationContextEventListener<RequestMappingMetadataReadyEvent>
        implements HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler, HandlerInterceptor {

    public static final String BEAN_NAME = "interceptingHandlerMethodProcessor";

    private static final Logger logger = LoggerFactory.getLogger(InterceptingHandlerMethodProcessor.class);

    private final Map<MethodParameter, MethodParameterContext> parameterContextsCache = new HashMap<>(256);

    private final Map<MethodParameter, ReturnTypeContext> returnTypeContextsCache = new HashMap<>(256);

    @Nullable
    private HandlerMethodAdvice handlerMethodAdvice;

    private List<HandlerMethodArgumentResolverAdvice> advices = emptyList();

    private int advicesSize = 0;

    static class MethodParameterContext {

        private HandlerMethod method;

        private int parameterCount;

        private HandlerMethodArgumentResolver resolver;

    }

    static class ReturnTypeContext {

        private HandlerMethod method;

        private HandlerMethodReturnValueHandler handler;

    }

    @Override
    protected void onApplicationContextEvent(RequestMappingMetadataReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initAdvices(context);
        initHandlerMethodAdvice(context);
        initRequestMappingHandlerAdapters(event, context);
    }

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
        MethodParameterContext methodParameterContext = getParameterContext(parameter);
        if (methodParameterContext == null) {
            logger.debug("The MethodParameterContext can't be found by the MethodParameter[{}]", parameter);
            return null;
        }

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
        if (context == null) {
            logger.debug("The ReturnTypeContext can't be found by the return type[{}]", returnType);
            return;
        }

        HandlerMethodReturnValueHandler handler = context.handler;

        HandlerMethod handlerMethod = context.method;

        afterExecute(webRequest, handlerMethod, context);

        handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // TODO NOTHING
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception error) throws Exception {
        if (error != null) {
            afterExecute(request, handler, error);
        }
    }

    private void initAdvices(ApplicationContext context) {
        List<HandlerMethodArgumentResolverAdvice> advices = getSortedBeans(context, HandlerMethodArgumentResolverAdvice.class);
        this.advices = advices;
        this.advicesSize = advices.size();
    }

    private void initHandlerMethodAdvice(ApplicationContext context) {
        this.handlerMethodAdvice = getOptionalBean(context, HandlerMethodAdvice.class);
    }

    private void initRequestMappingHandlerAdapters(RequestMappingMetadataReadyEvent event, ApplicationContext context) {
        List<RequestMappingMetadata> metadata = event.getMetadata();
        List<RequestMappingHandlerAdapter> adapters = getSortedBeans(context, RequestMappingHandlerAdapter.class);
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
            MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
            Method method = handlerMethod.getMethod();
            int parameterCount = method.getParameterCount();
            for (int i = 0; i < parameterCount; i++) {
                MethodParameter methodParameter = methodParameters[i];
                initMethodParameterContextsCache(methodParameter, handlerMethod, parameterCount, resolvers);
            }
            initReturnTypeContextsCache(handlerMethod, handlers);
        }
        adapter.setArgumentResolvers(singletonList(this));
        adapter.setReturnValueHandlers(singletonList(this));
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
        return parameterContextsCache.get(parameter);
    }

    private ReturnTypeContext getReturnTypeContext(MethodParameter returnType) {
        return returnTypeContextsCache.get(returnType);
    }

    private HandlerMethod resolveHandlerMethod(Object handler) {
        return handler instanceof HandlerMethod ? (HandlerMethod) handler : null;
    }

    private void beforeResolveArgument(MethodParameter parameter, MethodParameterContext methodParameterContext,
                                       ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                       WebDataBinderFactory binderFactory) throws Exception {
        for (int i = 0; i < advicesSize; i++) {
            HandlerMethodArgumentResolverAdvice advice = advices.get(i);
            advice.beforeResolveArgument(parameter, mavContainer, webRequest, binderFactory);
        }

        HandlerMethodAdvice facade = this.handlerMethodAdvice;
        if (facade != null) {
            HandlerMethod handlerMethod = methodParameterContext.method;
            facade.beforeResolveArgument(parameter, handlerMethod, webRequest);
        }
    }

    private void afterResolveArgument(MethodParameter parameter, Object argument, MethodParameterContext methodParameterContext,
                                      ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) throws Exception {
        for (int i = 0; i < advicesSize; i++) {
            HandlerMethodArgumentResolverAdvice advice = advices.get(i);
            advice.afterResolveArgument(parameter, argument, mavContainer, webRequest, binderFactory);
        }

        HandlerMethodAdvice facade = this.handlerMethodAdvice;
        if (facade != null) {
            HandlerMethod handlerMethod = methodParameterContext.method;
            facade.afterResolveArgument(parameter, argument, handlerMethod, webRequest);
        }
    }


    private void beforeExecute(MethodParameter parameter, MethodParameterContext methodParameterContext,
                               NativeWebRequest webRequest, Object argument) throws Exception {
        HandlerMethodAdvice facade = this.handlerMethodAdvice;
        if (facade != null) {
            int parameterCount = methodParameterContext.parameterCount;
            Object[] arguments = resolveArguments(parameter, argument, webRequest);

            int parameterIndex = parameter.getParameterIndex();
            if (parameterIndex == parameterCount - 1) {
                HandlerMethod handlerMethod = methodParameterContext.method;
                facade.beforeExecuteMethod(handlerMethod, arguments, webRequest);
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

    private void afterExecute(HttpServletRequest request, Object handler, @Nullable Exception error) throws Exception {
        HandlerMethod handlerMethod = resolveHandlerMethod(handler);
        if (handlerMethod != null) {
            ServletWebRequest webRequest = new ServletWebRequest(request);
            Object[] arguments = getArguments(request, handlerMethod);
            afterExecute(webRequest, handlerMethod, arguments, null, error);
        }
    }

    private void afterExecute(NativeWebRequest webRequest, HandlerMethod handlerMethod, Object[] arguments,
                              @Nullable Object returnValue, @Nullable Exception error) throws Exception {
        HandlerMethodAdvice facade = this.handlerMethodAdvice;
        if (facade != null) {
            facade.afterExecuteMethod(handlerMethod, arguments, returnValue, error, webRequest);
        }
    }

    private Object[] getArguments(NativeWebRequest webRequest, HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(webRequest, handlerMethod);
    }

    private Object[] getArguments(HttpServletRequest request, HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(request, handlerMethod);
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

    private HandlerMethodReturnValueHandler resolveReturnValueHandler(HandlerMethod methodParameter, List<HandlerMethodReturnValueHandler> handlers) {
        MethodParameter returnType = methodParameter.getReturnType();
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
                            getClassName(handler), methodParameter, getClassName(targetHandler));
                }
            }
        }
        return targetHandler;
    }

    private String getClassName(Object instance) {
        return instance.getClass().getName();
    }
}
