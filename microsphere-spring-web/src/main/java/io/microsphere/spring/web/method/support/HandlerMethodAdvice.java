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
package io.microsphere.spring.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * Intercepting {@link HandlerMethod} facade interface
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodInterceptor
 * @see HandlerMethodArgumentInterceptor
 * @since 1.0.0
 */
public interface HandlerMethodAdvice {

    /**
     * callback before the {@link MethodParameter} being resolved
     *
     * @param parameter     the method parameter to resolve.
     * @param handlerMethod the method to handle
     * @param webRequest    the current request
     * @throws Exception in case of errors with the preparation of argument values
     */
    void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest)
            throws Exception;

    /**
     * callback after the {@link MethodParameter} being resolved
     *
     * @param parameter        the method parameter to resolve.
     * @param resolvedArgument the resolved argument
     * @param handlerMethod    the method to handle
     * @param webRequest       the current request
     * @return the resolved argument value, or {@code null} if not resolvable
     * @throws Exception in case of errors with the preparation of argument values
     */
    void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod,
                              NativeWebRequest webRequest) throws Exception;

    /**
     * Interception point before the execution of a {@link HandlerMethod}. Called after
     * HandlerMapping determined an appropriate handler object, but before
     * HandlerAdapter invokes the handler.
     *
     * @param handlerMethod {@link HandlerMethod}
     * @param args          the resolved arguments of {@link HandlerMethod}
     * @param request       {@link WebRequest}
     * @throws Exception if any error caused
     */
    void beforeExecuteMethod(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception;

    /**
     * Interception point after successful execution of a {@link HandlerMethod}.
     * Called after HandlerAdapter actually invoked the handler.
     *
     * @param handlerMethod {@link HandlerMethod}
     * @param args          the resolved arguments of {@link HandlerMethod}
     * @param returnValue   the return value of {@link HandlerMethod}
     * @param error         the error after {@link HandlerMethod} invocation
     * @param request       {@link WebRequest}
     * @throws Exception if any error caused
     */
    void afterExecuteMethod(HandlerMethod handlerMethod, Object[] args, @Nullable Object returnValue, @Nullable Throwable error,
                            NativeWebRequest request) throws Exception;

}
