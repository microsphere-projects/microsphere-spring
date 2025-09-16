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

import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.web.util.WebType.SERVLET;

/**
 * The enumeration of the type of Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebType
 * @since 1.0.0
 */
public enum SpringWebType {

    WEB_MVC,

    WEB_FLUX;

    /**
     * The class name of the indicator of Spring WebMVC
     *
     * @see org.springframework.web.servlet.DispatcherServlet
     */
    public static final String WEBMVC_INDICATOR_CLASS_NAME = "org.springframework.web.servlet.DispatcherServlet";

    /**
     * The class name of the indicator of Spring WebFlux
     *
     * @see org.springframework.web.reactive.DispatcherHandler
     */
    public static final String WEBFLUX_INDICATOR_CLASS_NAME = "org.springframework.web.reactive.DispatcherHandler";

    /**
     * Resolve the {@link SpringWebType} from the given {@link NativeWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = ...;
     * SpringWebType type = SpringWebType.valueOf(request);
     * switch (type) {
     *     case WEB_MVC:
     *         // Handle Spring MVC request
     *         break;
     *     case WEB_FLUX:
     *         // Handle Spring WebFlux request
     *         break;
     * }
     * }</pre>
     * </p>
     *
     * @param request the {@link NativeWebRequest} to resolve
     * @return the {@link SpringWebType} of the given {@link NativeWebRequest}
     * @throws IllegalArgumentException if the {@link NativeWebRequest} is null
     * @see NativeWebRequest
     */
    public static SpringWebType valueOf(NativeWebRequest request) {
        WebType webType = WebType.valueOf(request);
        return webType == SERVLET ? WEB_MVC : WEB_FLUX;
    }
}