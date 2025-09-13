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

package io.microsphere.spring.webmvc.interceptor;

import io.microsphere.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link MethodHandlerInterceptor} for Logging
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodHandlerInterceptor
 * @since 1.0.0
 */
public class LoggingMethodHandlerInterceptor extends MethodHandlerInterceptor {

    private static final Logger logger = getLogger(LoggingMethodHandlerInterceptor.class);

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        logger.trace("preHandle - handlerMethod : {}", handlerMethod);
        return true;
    }

    @Override
    protected void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView) {
        logger.trace("postHandle - handlerMethod : {} , modelAndView : {}", handlerMethod, modelAndView);
    }

    @Override
    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) {
        logger.trace("afterCompletion - handlerMethod : {} , exception : {}", handlerMethod, ex);
    }
}
