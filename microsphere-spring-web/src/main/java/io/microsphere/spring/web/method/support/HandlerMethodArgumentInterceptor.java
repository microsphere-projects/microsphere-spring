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
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;


/**
 * The interceptor interface for the resolvable {@link HandlerMethod HandlerMethods'} {@link MethodParameter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethod
 * @see MethodParameter
 * @since 1.0.0
 */
public interface HandlerMethodArgumentInterceptor {

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


}
