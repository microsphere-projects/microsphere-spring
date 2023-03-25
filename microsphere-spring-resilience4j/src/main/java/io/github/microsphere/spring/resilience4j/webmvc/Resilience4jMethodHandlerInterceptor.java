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
package io.github.microsphere.spring.resilience4j.webmvc;

import io.github.microsphere.spring.webmvc.interceptor.MethodHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The abstract template class for Resilience4j's {@link MethodHandlerInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodHandlerInterceptor
 * @since 1.0.0
 */
public class Resilience4jMethodHandlerInterceptor extends MethodHandlerInterceptor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Resilience4jMethodHandlerInterceptor() {
        // always keep self being a delegate
        super(Boolean.TRUE);
    }

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        return false;
    }

    @Override
    protected void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView) throws Exception {

    }

    @Override
    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) throws Exception {

    }

    protected String getResource(HttpServletRequest request, HandlerMethod handlerMethod) {
        return null;
    }

}
