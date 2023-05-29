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

import io.microsphere.spring.webmvc.IdempotentException;
import io.microsphere.spring.webmvc.annotation.Idempotent;
import io.microsphere.spring.webmvc.method.HandlerMethodArgumentsResolvedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * {@link AnnotatedMethodHandlerInterceptor} for {@link Idempotent} annotation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class IdempotentAnnotatedMethodHandlerInterceptor extends AnnotatedMethodHandlerInterceptor<Idempotent> implements ApplicationListener<HandlerMethodArgumentsResolvedEvent> {

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                Idempotent idempotent) throws Exception {

        System.out.println(handlerMethod);
        System.out.println(idempotent);

        return true;
    }

    // Handler Methods' arguments Resolved
    // Handler Method is not executed
    @Override
    public void onApplicationEvent(HandlerMethodArgumentsResolvedEvent event) {
        Method method = event.getMethod();
        Object[] args = event.getArguments();
        System.out.println("method : " + method + " , args : " + Arrays.asList(args));
        WebRequest webRequest = event.getWebRequest();
        if (webRequest instanceof ServletWebRequest) {
            ServletWebRequest servletWebRequest = (ServletWebRequest) webRequest;
            HttpServletRequest request = servletWebRequest.getNativeRequest(HttpServletRequest.class);
            // HttpSession based on Spring Redis
            // Spring Session
            HttpSession httpSession = request.getSession();
            String token = request.getHeader("token");
            Object tokenValue = httpSession.getAttribute(token);
            if (tokenValue != null) {
                //
                throw new IdempotentException("");
            }
        }
    }
}
