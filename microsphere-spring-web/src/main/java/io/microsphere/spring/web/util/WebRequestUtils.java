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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.web.util.SpringWebType.valueOf;
import static io.microsphere.spring.web.util.UnknownSpringWebHelper.INSTANCE;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.util.StringUtils.hasLength;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link WebRequest} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class WebRequestUtils {

    private static EnumMap<SpringWebType, SpringWebHelper> springWebHelpers = loadSpringWebHelpers();

    /**
     * The header name for HTTP method
     */
    public static final String METHOD_HEADER_NAME = ":METHOD:";

    /**
     * Name of Servlet request attribute that holds a
     * {@link UrlPathHelper#getLookupPathForRequest resolved} lookupPath.
     *
     * @since Spring Framework 5.3
     */
    public static final String PATH_ATTRIBUTE = UrlPathHelper.class.getName() + ".PATH";

    /**
     * Get the HTTP method of the current request.
     *
     * @param request the {@link NativeWebRequest}
     * @return the HTTP method if found, otherwise <code>null</code>
     * @see HttpServletRequest#getMethod()
     * @see HttpRequest#getMethod()
     */
    @Nullable
    public static String getMethod(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getMethod(request);
    }

    /**
     * Returns {@code true} if the request is a valid CORS pre-flight one by checking {@code OPTIONS} method with
     * {@code Origin} and {@code Access-Control-Request-Method} headers presence.
     */
    public static boolean isPreFlightRequest(@Nonnull NativeWebRequest request) {
        String method = getMethod(request);
        return (HttpMethod.OPTIONS.matches(method) &&
                request.getHeader(ORIGIN) != null &&
                request.getHeader(ACCESS_CONTROL_REQUEST_METHOD) != null);
    }

    public static String getContentType(@Nonnull NativeWebRequest request) {
        return request.getHeader(CONTENT_TYPE);
    }

    @Nullable
    public static MediaType parseContentType(@Nonnull NativeWebRequest request) {
        MediaType mediaType = null;
        try {
            String contentTypeValue = getContentType(request);
            mediaType = hasLength(contentTypeValue) ? parseMediaType(contentTypeValue) : APPLICATION_OCTET_STREAM;
        } catch (InvalidMediaTypeException ex) {
        }
        return mediaType;
    }

    public static boolean hasBody(@Nonnull NativeWebRequest request) {
        String contentLength = request.getHeader(CONTENT_LENGTH);
        String transferEncoding = request.getHeader(TRANSFER_ENCODING);
        return hasText(transferEncoding) ||
                (hasText(contentLength) && !contentLength.trim().equals("0"));
    }

    /**
     * Return a previously {@link UrlPathHelper#getLookupPathForRequest resolved} lookupPath.
     *
     * @param request the current request
     * @return the previously resolved lookupPath
     * @throws IllegalArgumentException if the lookup path is not found
     */
    public static String getResolvedLookupPath(@Nonnull NativeWebRequest request) {
        String lookupPath = REQUEST.getAttribute(request, PATH_ATTRIBUTE);
        return lookupPath;
    }

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
    public static String getHeader(@Nonnull NativeWebRequest request, String headerName) {
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
    public static String[] getHeaderValues(@Nonnull NativeWebRequest request, String headerName) {
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
    public static void setHeader(@Nonnull NativeWebRequest request, @Nullable String headerName, @Nullable String headerValue) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        springWebHelper.setHeader(request, headerName, headerValue);
    }

    /**
     * Adds a response header with the given name and value. This method allows response headers to have multiple values.
     *
     * @param request      the {@link NativeWebRequest}
     * @param headerName   the name of header
     * @param headerValues the header values
     * @see javax.servlet.http.HttpServletResponse#addHeader(String, String)
     * @see HttpHeaders#add(String, String)
     */
    public static void addHeader(@Nonnull NativeWebRequest request, @Nullable String headerName, @Nullable String... headerValues) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        springWebHelper.addHeader(request, headerName, headerValues);
    }

    /**
     * Get the cookie value for the given cookie name.
     *
     * @param request    the {@link NativeWebRequest}
     * @param cookieName the name of Cookie
     * @return the cookie value if found, otherwise <code>null</code>
     */
    @Nullable
    public static String getCookieValue(@Nonnull NativeWebRequest request, String cookieName) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getCookieValue(request, cookieName);
    }

    /**
     * Adds the specified cookie to the response. This method can be called multiple times to set more than one cookie.
     *
     * @param request     the {@link NativeWebRequest}
     * @param cookieName  the name of Cookie
     * @param cookieValue the cookie value
     * @see javax.servlet.http.HttpServletResponse#addCookie(Cookie)
     * @see org.springframework.http.server.reactive.ServerHttpResponse#addCookie(ResponseCookie)
     */
    public static void addCookie(@Nonnull NativeWebRequest request, String cookieName, String cookieValue) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        springWebHelper.addCookie(request, cookieName, cookieValue);
    }

    @Nullable
    public static <T> T getRequestBody(@Nonnull NativeWebRequest request, Class<T> requestBodyType) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getRequestBody(request, requestBodyType);
    }

    /**
     * Write value to the response body.
     *
     * @param request the {@link NativeWebRequest}
     * @param name    the name
     * @param value   the value
     */
    public static void writeResponseBody(@Nonnull NativeWebRequest request, String name, String value) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        springWebHelper.writeResponseBody(request, name, value);
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
    public static Object getBestMatchingHandler(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getBestMatchingHandler(request);
    }

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
    public static String getPathWithinHandlerMapping(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getPathWithinHandlerMapping(request);
    }

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
    public static String getBestMatchingPattern(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getBestMatchingPattern(request);
    }

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
    public static Map<String, String> getUriTemplateVariables(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getUriTemplateVariables(request);
    }

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
    public static Map<String, MultiValueMap<String, String>> getMatrixVariables(NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getMatrixVariables(request);
    }

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
    public static Set<MediaType> getProducibleMediaTypes(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getProducibleMediaTypes(request);
    }

    private static EnumMap<SpringWebType, SpringWebHelper> loadSpringWebHelpers() {
        EnumMap<SpringWebType, SpringWebHelper> springWebHelpersMap = new EnumMap<>(SpringWebType.class);
        ClassLoader classLoader = getClassLoader(SpringWebHelper.class);
        List<SpringWebHelper> springWebHelpersList = loadFactories(SpringWebHelper.class, classLoader);
        springWebHelpersList.forEach(helper -> springWebHelpersMap.put(helper.getType(), helper));
        return springWebHelpersMap;
    }

    protected static SpringWebHelper getSpringWebHelper(NativeWebRequest request) {
        SpringWebType springWebType = valueOf(request);
        return springWebHelpers.getOrDefault(springWebType, INSTANCE);
    }

    private WebRequestUtils() {
    }
}