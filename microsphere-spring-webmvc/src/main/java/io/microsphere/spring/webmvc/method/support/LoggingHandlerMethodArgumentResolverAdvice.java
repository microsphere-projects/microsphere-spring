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

import io.microsphere.logging.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link HandlerMethodArgumentResolverAdvice} for logging
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodArgumentResolverAdvice
 * @since 1.0.0
 */
public class LoggingHandlerMethodArgumentResolverAdvice implements HandlerMethodArgumentResolverAdvice {

    private static final Logger logger = getLogger(LoggingHandlerMethodArgumentResolverAdvice.class);

    @Override
    public void beforeResolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        logger.trace("beforeResolveArgument - parameter : {} , mavContainer : {} , webRequest : {} , binderFactory : {}",
                parameter, mavContainer, webRequest, binderFactory);
    }

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, ModelAndViewContainer mavContainer,
                                     NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        logger.trace("afterResolveArgument - parameter : {} , resolvedArgument : {} , mavContainer : {} , webRequest : {} , binderFactory : {}",
                parameter, mavContainer, webRequest, binderFactory);
    }
}
