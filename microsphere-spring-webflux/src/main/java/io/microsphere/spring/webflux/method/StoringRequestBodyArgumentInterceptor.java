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

import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.util.RequestAttributesUtils;
import io.microsphere.spring.webflux.annotation.EnableWebFluxExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodRequestBodyArgument;

/**
 * The {@link HandlerMethodArgumentInterceptor} class stores the {@link MethodParameter argument} of {@link HandlerMethod} that annotated {@link RequestBody}
 * into {@link RequestAttributes}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebFluxExtension#storeRequestBodyArgument()
 * @see RequestAttributesUtils#setHandlerMethodRequestBodyArgument(RequestAttributes, Method, Object)
 * @see RequestAttributesUtils#getHandlerMethodRequestBodyArgument(RequestAttributes, HandlerMethod)
 * @see HandlerMethodArgumentInterceptor
 * @see RequestBody
 * @since 1.0.0
 */
public class StoringRequestBodyArgumentInterceptor implements HandlerMethodArgumentInterceptor {

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod,
                                     NativeWebRequest webRequest) throws Exception {
        if (parameter.getParameter().isAnnotationPresent(RequestBody.class)) {
            setHandlerMethodRequestBodyArgument(webRequest, handlerMethod.getMethod(), resolvedArgument);
        }
    }
}
