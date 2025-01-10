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
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.webmvc.IdempotentException;
import io.microsphere.spring.webmvc.annotation.Idempotent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Arrays.asList;

/**
 * {@link AnnotatedMethodHandlerInterceptor} for {@link Idempotent} annotation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class IdempotentAnnotatedMethodHandlerInterceptor extends AnnotatedMethodHandlerInterceptor<Idempotent>
        implements ApplicationListener<HandlerMethodArgumentsResolvedEvent> {

    private static final Logger logger = getLogger(IdempotentAnnotatedMethodHandlerInterceptor.class);

    public static final String TOKEN_HEADER_NAME = "_token_";

    public static final String MOCK_TOKEN_VALUE = UUID.randomUUID().toString();

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                Idempotent idempotent) throws Exception {
        return true;
    }

    // Handler Methods' arguments Resolved
    // Handler Method is not executed
    @Override
    public void onApplicationEvent(HandlerMethodArgumentsResolvedEvent event) {
        Method method = event.getMethod();
        Object[] args = event.getArguments();
        WebRequest webRequest = event.getWebRequest();
        logger.trace("The method : {} , args : {} , webRequest : {}", method, asList(args), webRequest);
        if (webRequest instanceof ServletWebRequest) {
            ServletWebRequest servletWebRequest = (ServletWebRequest) webRequest;
            HttpServletRequest request = servletWebRequest.getNativeRequest(HttpServletRequest.class);
            String token = request.getHeader(TOKEN_HEADER_NAME);
            if (!Objects.equals(MOCK_TOKEN_VALUE, token)) {
                throw new IdempotentException("Illegal token");
            }
        }
    }
}
