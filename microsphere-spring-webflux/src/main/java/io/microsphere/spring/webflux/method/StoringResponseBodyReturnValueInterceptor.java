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

import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.web.util.RequestAttributesUtils;
import io.microsphere.spring.webflux.annotation.EnableWebFluxExtension;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodReturnValue;

/**
 * The {@link HandlerMethodInterceptor} class stores the return value of {@link HandlerMethod} before write as the {@link ResponseBody}
 * into {@link RequestAttributes}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebFluxExtension#storeResponseBodyReturnValue()
 * @see RequestAttributesUtils#setHandlerMethodReturnValue(RequestAttributes, Method, Object)
 * @see RequestAttributesUtils#getHandlerMethodReturnValue(RequestAttributes, HandlerMethod)
 * @see HandlerMethodInterceptor
 * @see ResponseBody
 * @since 1.0.0
 */
public class StoringResponseBodyReturnValueInterceptor implements HandlerMethodInterceptor {

    @Override
    public void afterExecute(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) throws Exception {
        if (returnValue != null && handlerMethod.hasMethodAnnotation(ResponseBody.class)) {
            setHandlerMethodReturnValue(request, handlerMethod.getMethod(), returnValue);
        }
    }
}
