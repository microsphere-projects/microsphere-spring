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
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UrlPathHelper;

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

    public static String getMethod(@Nonnull NativeWebRequest request) {
        String method = request.getHeader(METHOD_HEADER_NAME);
        if (method == null) {
            if (request instanceof ServletWebRequest) {
                Object nativeRequest = request.getNativeRequest();
                HttpServletRequest servletRequest = (HttpServletRequest) nativeRequest;
                method = servletRequest.getMethod();
            }
        }
        return method;
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
     * Get the best matching handler
     *
     * @param request the {@link NativeWebRequest}
     * @return the best matching handler
     * @throws IllegalArgumentException if the {@link NativeWebRequest} is null
     * @see HandlerMethod
     * @see org.springframework.web.servlet.HandlerMapping#getHandler(HttpServletRequest)
     * @see org.springframework.web.reactive.HandlerMapping#getHandler(ServerWebExchange)
     * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
     */
    @Nullable
    public static Object getBestMatchingHandler(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getBestMatchingHandler(request);
    }

    /**
     * Get the path within handler mapping
     *
     * @param request the {@link NativeWebRequest}
     * @return the path within handler mapping
     * @throws IllegalArgumentException if the {@link NativeWebRequest} is null
     * @see org.springframework.web.servlet.HandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
     */
    @Nullable
    public static String getPathWithinHandlerMapping(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getPathWithinHandlerMapping(request);
    }

    /**
     * Get the best matching pattern
     *
     * @param request the {@link NativeWebRequest}
     * @return the best matching pattern
     * @throws IllegalArgumentException if the {@link NativeWebRequest} is null
     * @see RequestMapping
     * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE
     */
    @Nullable
    public static String getBestMatchingPattern(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getBestMatchingPattern(request);
    }

    /**
     * Get the URI template variables
     *
     * @param request the {@link NativeWebRequest}
     * @return the URI template variables
     * @throws IllegalArgumentException if the {@link NativeWebRequest} is null
     * @see RequestMapping#path()
     * @see PathVariable
     * @see org.springframework.web.servlet.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
     * @see org.springframework.web.reactive.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
     */
    @Nullable
    public static Map<String, String> getUriTemplateVariables(@Nonnull NativeWebRequest request) {
        SpringWebHelper springWebHelper = getSpringWebHelper(request);
        return springWebHelper.getUriTemplateVariables(request);
    }

    /**
     * Get the producible media types
     *
     * @param request the {@link NativeWebRequest}
     * @return the producible media types
     * @throws IllegalArgumentException if the {@link NativeWebRequest} is null
     * @see RequestMapping#produces()
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
