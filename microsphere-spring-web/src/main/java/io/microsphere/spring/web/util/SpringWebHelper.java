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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.Set;

/**
 * The helper interface for Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequest
 * @since 1.0.0
 */
public interface SpringWebHelper {

    /**
     * Get the best matching handler
     *
     * @param request the {@link NativeWebRequest}
     * @return the best matching handler
     * @see HandlerMethod
     * @see org.springframework.web.servlet.HandlerMapping#getHandler(HttpServletRequest)
     * @see org.springframework.web.reactive.HandlerMapping#getHandler(ServerWebExchange)
     * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     */
    Object getBestMatchingHandler(NativeWebRequest request);

    /**
     * Get the path within handler mapping
     *
     * @param request the {@link NativeWebRequest}
     * @return the path within handler mapping
     * @see org.springframework.web.servlet.HandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     */
    String getPathWithinHandlerMapping(NativeWebRequest request);

    /**
     * Get the best matching pattern
     *
     * @param request the {@link NativeWebRequest}
     * @return the best matching pattern
     * @see RequestMapping
     * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE
     */
    String getBestMatchingPattern(NativeWebRequest request);

    /**
     * Get the URI template variables
     *
     * @param request the {@link NativeWebRequest}
     * @return the URI template variables
     * @see RequestMapping#path()
     * @see PathVariable
     * @see org.springframework.web.servlet.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
     */
    Map<String, String> getUriTemplateVariables(NativeWebRequest request);

    /**
     * Get the producible media types
     *
     * @param request the {@link NativeWebRequest}
     * @return the producible media types
     * @see RequestMapping#produces()
     * @see org.springframework.web.servlet.HandlerMapping#PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
     */
    Set<MediaType> getProducibleMediaTypes(NativeWebRequest request);
}
