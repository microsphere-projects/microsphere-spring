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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodRequestBodyArgument;

/**
 * The helper interface for Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequest
 * @since 1.0.0
 */
public interface SpringWebHelper {

    /**
     * Get the HTTP method of the request
     *
     * @param request the {@link NativeWebRequest}
     * @return the HTTP method if found, otherwise <code>null</code>
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     * @see HttpRequest#getMethod()
     */
    @Nullable
    String getMethod(@Nonnull NativeWebRequest request);

    /**
     * Get the header value from the request by the given header name.
     *
     * @param request    the {@link NativeWebRequest}
     * @param headerName the name of header
     * @return the header value if found, otherwise <code>null</code>
     * @see javax.servlet.http.HttpServletRequest#getHeader(String)
     * @see HttpMessage#getHeaders()
     * @see HttpHeaders#getFirst(String)
     */
    @Nullable
    default String getHeader(@Nonnull NativeWebRequest request, String headerName) {
        return request.getHeader(headerName);
    }

    /**
     * Get the header values from the request by the given header name.
     *
     * @param request    the {@link NativeWebRequest}
     * @param headerName the name of header
     * @return the header values if found, otherwise <code>null</code>
     * @see javax.servlet.http.HttpServletRequest#getHeaders(String)
     * @see HttpMessage#getHeaders()
     * @see HttpHeaders#get(Object)
     */
    @Nullable
    default String[] getHeaderValues(@Nonnull NativeWebRequest request, String headerName) {
        return request.getHeaderValues(headerName);
    }

    /**
     * Sets a response header with the given name and value. If the header had already been set,
     * the new value overwrites all previous values.
     *
     * @param request     the {@link NativeWebRequest}
     * @param headerName  the name of header
     * @param headerValue the header value
     * @see javax.servlet.http.HttpServletResponse#setHeader(String, String)
     * @see HttpHeaders#set(String, String)
     */
    void setHeader(@Nonnull NativeWebRequest request, @Nullable String headerName, @Nullable String headerValue);

    /**
     * Adds a response header with the given name and value. This method allows response headers to have multiple values.
     *
     * @param request      the {@link NativeWebRequest}
     * @param headerName   the name of header
     * @param headerValues the header values
     * @see javax.servlet.http.HttpServletResponse#addHeader(String, String)
     * @see HttpHeaders#add(String, String)
     */
    void addHeader(@Nonnull NativeWebRequest request, @Nullable String headerName, @Nullable String... headerValues);

    /**
     * Get the cookie value for the given cookie name.
     *
     * @param request    the {@link NativeWebRequest}
     * @param cookieName the name of Cookie
     * @return the cookie value if found, otherwise <code>null</code>
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     * @see org.springframework.http.server.reactive.ServerHttpRequest#getCookies()
     */
    @Nullable
    String getCookieValue(@Nonnull NativeWebRequest request, String cookieName);

    /**
     * Adds the specified cookie to the response. This method can be called multiple times to set more than one cookie.
     *
     * @param request     the {@link NativeWebRequest}
     * @param cookieName  the name of Cookie
     * @param cookieValue the cookie value
     * @see javax.servlet.http.HttpServletResponse#addCookie(Cookie)
     * @see org.springframework.http.server.reactive.ServerHttpResponse#addCookie
     */
    void addCookie(@Nonnull NativeWebRequest request, String cookieName, String cookieValue);

    /**
     * Get the request body for the given request body type.
     *
     * @param request         the {@link NativeWebRequest}
     * @param requestBodyType the request body type
     * @param <T>             the type of request body
     * @return the request body if found, otherwise <code>null</code>
     * @see RequestBody
     * @see org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor#resolveArgument
     * @see org.springframework.web.reactive.result.method.annotation.RequestBodyMethodArgumentResolver#resolveArgument
     */
    default <T> T getRequestBody(@Nonnull NativeWebRequest request, Class<T> requestBodyType) {
        Object handler = getBestMatchingHandler(request);
        if (handler instanceof HandlerMethod) {
            return getHandlerMethodRequestBodyArgument(request, (HandlerMethod) handler);
        }
        return null;
    }

    /**
     * Write value to the response body.
     *
     * @param request the {@link NativeWebRequest}
     * @param name    the name
     * @param value   the value
     */
    default void writeResponseBody(@Nonnull NativeWebRequest request, String name, String value) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /**
     * Get the mapped handler for the best matching pattern.
     *
     * @param request the {@link NativeWebRequest}
     * @return the mapped handler for the best matching pattern if found, otherwise <code>null</code>
     * @see HandlerMethod
     * @see org.springframework.web.servlet.function.RouterFunction
     * @see org.springframework.web.reactive.function.server.RouterFunction
     * @see org.springframework.web.servlet.HandlerMapping#getHandler
     * @see org.springframework.web.reactive.HandlerMapping#getHandler
     * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     */
    @Nullable
    Object getBestMatchingHandler(@Nonnull NativeWebRequest request);

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
    String getPathWithinHandlerMapping(@Nonnull NativeWebRequest request);

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
    String getBestMatchingPattern(@Nonnull NativeWebRequest request);

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
    Map<String, String> getUriTemplateVariables(@Nonnull NativeWebRequest request);

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
    Map<String, MultiValueMap<String, String>> getMatrixVariables(@Nonnull NativeWebRequest request);

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
    Set<MediaType> getProducibleMediaTypes(@Nonnull NativeWebRequest request);

    /**
     * Get the Spring Web Type
     *
     * @return {@link SpringWebType}
     */
    @Nonnull
    SpringWebType getType();
}