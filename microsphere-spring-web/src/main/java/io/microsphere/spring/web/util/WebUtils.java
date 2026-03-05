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

package io.microsphere.spring.web.util;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.util.ArrayUtils.isEmpty;

/**
 * The utilties class for Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethod
 * @since 1.0.0
 */
public abstract class WebUtils implements Utils {

    /**
     * Determine whether the specified handler is {@link HandlerMethod}
     *
     * @param handler the specified handler
     * @return if the specified handler is {@link HandlerMethod}, return <code>true</code>, or <code>false</code>
     */
    public static boolean isHandlerMethod(@Nullable Object handler) {
        return handler instanceof HandlerMethod;
    }

    /**
     * Determine whether the specified handler is {@link HandlerMethod} and no argument
     *
     * @param handler the specified handler
     * @return if the specified handler is {@link HandlerMethod} and no argument, return <code>true</code>, or <code>false</code>
     */
    public static boolean isNoArgumentHandlerMethod(@Nullable Object handler) {
        if (isHandlerMethod(handler)) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            return isNoArgumentHandlerMethod(handlerMethod);
        }
        return false;
    }

    /**
     * Determine whether the specified {@link HandlerMethod} is no argument
     *
     * @param handlerMethod the specified {@link HandlerMethod}
     * @return if the specified {@link HandlerMethod} is no argument, return <code>true</code>, or <code>false</code>
     */
    public static boolean isNoArgumentHandlerMethod(@Nullable HandlerMethod handlerMethod) {
        return handlerMethod == null ? false : isEmpty(handlerMethod.getMethodParameters());
    }

    private WebUtils() {
    }
}