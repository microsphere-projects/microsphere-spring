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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.MatrixVariable;
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
     * Get the cookie value for the given cookie name.
     *
     * @param request    the {@link NativeWebRequest}
     * @param cookieName the name of Cookie
     * @return the cookie value if found, otherwise <code>null</code>
     */
    @Nullable
    String getCookieValue(NativeWebRequest request, String cookieName);

    /**
     * Get the mapped handler for the best matching pattern.
     *
     * @param request the {@link NativeWebRequest}
     * @return the mapped handler for the best matching pattern if found, otherwise <code>null</code>
     * @see HandlerMethod
     * @see org.springframework.web.servlet.function.RouterFunction
     * @see org.springframework.web.reactive.function.server.RouterFunction
     * @see org.springframework.web.servlet.HandlerMapping#getHandler(HttpServletRequest)
     * @see org.springframework.web.reactive.HandlerMapping#getHandler(ServerWebExchange)
     * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     */
    @Nullable
    Object getBestMatchingHandler(NativeWebRequest request);

    /**
     * Get the path within the handler mapping, in case of a pattern match, or the full relevant URI
     * (typically within the DispatcherServlet's mapping) else.
     *
     * @param request the {@link NativeWebRequest}
     * @return the path within handler mapping if found, otherwise <code>null</code>
     * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#exposePathWithinMapping
     * @see org.springframework.web.reactive.handler.AbstractUrlHandlerMapping#lookupHandler
     * @see org.springframework.web.servlet.HandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     */
    @Nullable
    String getPathWithinHandlerMapping(NativeWebRequest request);

    /**
     * Get the best matching pattern within the handler mapping.
     *
     * @param request the {@link NativeWebRequest}
     * @return the best matching pattern within the handler mapping if found, otherwise <code>null</code>
     * @see RequestMapping#path()
     * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#exposePathWithinMapping
     * @see org.springframework.web.reactive.handler.AbstractUrlHandlerMapping#lookupHandler
     * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE
     */
    @Nullable
    String getBestMatchingPattern(NativeWebRequest request);

    /**
     * Get the URI templates map, mapping variable names to values.
     *
     * @param request the {@link NativeWebRequest}
     * @return the URI template variables if found, otherwise <code>null</code>
     * @see PathVariable
     * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#exposeUriTemplateVariables
     * @see org.springframework.web.reactive.handler.AbstractUrlHandlerMapping#lookupHandler
     * @see org.springframework.web.servlet.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
     */
    @Nullable
    Map<String, String> getUriTemplateVariables(NativeWebRequest request);

    /**
     * Get a map with URI variable names and a corresponding {@link MultiValueMap} of URI matrix variables for each.
     *
     * @param request the {@link NativeWebRequest}
     * @return the matrix variables if found, otherwise <code>null</code>
     * @see MatrixVariable
     * @see org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMapMethodArgumentResolver#resolveArgument
     * @see org.springframework.web.reactive.result.method.annotation.MatrixVariableMapMethodArgumentResolver#resolveArgumentValue
     * @see org.springframework.web.servlet.HandlerMapping#MATRIX_VARIABLES_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#MATRIX_VARIABLES_ATTRIBUTE
     */
    @Nullable
    Map<String, MultiValueMap<String, String>> getMatrixVariables(NativeWebRequest request);

    /**
     * Get the set of producible MediaTypes applicable to the mapped handler.
     *
     * @param request the {@link NativeWebRequest}
     * @return the producible media types if found, otherwise <code>null</code>
     * @see RequestMapping#produces()
     * @see org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping#handleMatch
     * @see org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping#handleMatch
     * @see org.springframework.web.servlet.HandlerMapping#PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
     */
    @Nullable
    Set<MediaType> getProducibleMediaTypes(NativeWebRequest request);

    /**
     * Get the Spring Web Type
     *
     * @return {@link SpringWebType}
     */
    @Nonnull
    SpringWebType getType();
}
