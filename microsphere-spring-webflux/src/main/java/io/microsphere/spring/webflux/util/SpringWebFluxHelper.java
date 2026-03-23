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

package io.microsphere.spring.webflux.util;

import io.microsphere.spring.web.util.SpringWebHelper;
import io.microsphere.spring.web.util.SpringWebType;
import io.microsphere.spring.webflux.context.request.ServerWebRequest;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.web.util.SpringWebType.WEB_FLUX;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static org.springframework.web.reactive.HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE;
import static org.springframework.web.reactive.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;
import static org.springframework.web.reactive.HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE;
import static org.springframework.web.reactive.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;
import static org.springframework.web.reactive.HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE;
import static org.springframework.web.reactive.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

/**
 * {@link SpringWebHelper} for Spring WebFlux
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringWebHelper
 * @since 1.0.0
 */
public class SpringWebFluxHelper implements SpringWebHelper {

    /**
     * Gets the HTTP method from the given {@link NativeWebRequest} by extracting it from
     * the underlying {@link ServerHttpRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   String method = helper.getMethod(serverWebRequest); // e.g. "GET"
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to extract the HTTP method from
     * @return the HTTP method string (e.g. "GET", "POST")
     */
    @Override
    public String getMethod(NativeWebRequest request) {
        ServerHttpRequest serverHttpRequest = getServerHttpRequest(request);
        return serverHttpRequest.getMethod().name();
    }

    /**
     * Sets a response header on the underlying {@link ServerHttpResponse}.
     * If the header already exists, its value is replaced.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   helper.setHeader(serverWebRequest, "Content-Type", "application/json");
     * }</pre>
     *
     * @param request     the {@link NativeWebRequest} wrapping the server response
     * @param headerName  the name of the header to set
     * @param headerValue the value of the header to set
     */
    @Override
    public void setHeader(NativeWebRequest request, String headerName, String headerValue) {
        HttpHeaders httpHeaders = getResponseHeaders(request);
        httpHeaders.set(headerName, headerValue);
    }

    /**
     * Adds one or more values to a response header on the underlying {@link ServerHttpResponse}.
     * Unlike {@link #setHeader}, this appends values rather than replacing existing ones.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   helper.addHeader(serverWebRequest, "Accept", "text/html", "application/json");
     * }</pre>
     *
     * @param request      the {@link NativeWebRequest} wrapping the server response
     * @param headerName   the name of the header to add values to
     * @param headerValues the values to add to the header
     */
    @Override
    public void addHeader(NativeWebRequest request, String headerName, String... headerValues) {
        HttpHeaders httpHeaders = getResponseHeaders(request);
        for (String headerValue : headerValues) {
            httpHeaders.add(headerName, headerValue);
        }
    }

    /**
     * Gets the value of a cookie with the specified name from the underlying {@link ServerHttpRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   String sessionId = helper.getCookieValue(serverWebRequest, "JSESSIONID");
     * }</pre>
     *
     * @param request    the {@link NativeWebRequest} wrapping the server request
     * @param cookieName the name of the cookie to retrieve
     * @return the cookie value, or {@code null} if the cookie is not found
     */
    @Override
    public String getCookieValue(NativeWebRequest request, String cookieName) {
        ServerHttpRequest serverHttpRequest = getServerHttpRequest(request);
        MultiValueMap<String, HttpCookie> cookies = serverHttpRequest.getCookies();
        HttpCookie cookie = cookies.getFirst(cookieName);
        return cookie == null ? null : cookie.getValue();
    }

    /**
     * Adds a cookie with the specified name and value to the underlying {@link ServerHttpResponse}.
     * The cookie is created with {@code secure} and {@code httpOnly} flags enabled.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   helper.addCookie(serverWebRequest, "session", "abc123");
     * }</pre>
     *
     * @param request     the {@link NativeWebRequest} wrapping the server response
     * @param cookieName  the name of the cookie to add
     * @param cookieValue the value of the cookie to add
     */
    @Override
    public void addCookie(NativeWebRequest request, String cookieName, String cookieValue) {
        ServerHttpResponse serverHttpResponse = getServerHttpResponse(request);
        ResponseCookie cookie = ResponseCookie.from(cookieName, cookieValue)
                .secure(true)
                .httpOnly(true)
                .build();
        serverHttpResponse.addCookie(cookie);
    }

    /**
     * Gets the best matching handler from the request attributes, as resolved by Spring WebFlux's
     * {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   Object handler = helper.getBestMatchingHandler(serverWebRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} containing handler mapping attributes
     * @return the best matching handler object, or {@code null} if not available
     */
    @Override
    public Object getBestMatchingHandler(NativeWebRequest request) {
        return REQUEST.getAttribute(request, BEST_MATCHING_HANDLER_ATTRIBUTE);
    }

    /**
     * Gets the path within the handler mapping from the request attributes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   String path = helper.getPathWithinHandlerMapping(serverWebRequest); // e.g. "/users/123"
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} containing handler mapping attributes
     * @return the path within the handler mapping, or {@code null} if not available
     */
    @Override
    public String getPathWithinHandlerMapping(NativeWebRequest request) {
        return REQUEST.getAttribute(request, PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

    /**
     * Gets the best matching URL pattern from the request attributes, as resolved by Spring WebFlux's
     * {@code HandlerMapping}. The {@link PathPattern} is converted to its string representation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   String pattern = helper.getBestMatchingPattern(serverWebRequest); // e.g. "/users/{id}"
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} containing handler mapping attributes
     * @return the best matching URL pattern string, or {@code null} if not available
     */
    @Override
    public String getBestMatchingPattern(NativeWebRequest request) {
        PathPattern pathPattern = REQUEST.getAttribute(request, BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pathPattern == null ? null : pathPattern.getPatternString();
    }

    /**
     * Gets the URI template variables from the request attributes, as resolved by Spring WebFlux's
     * {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   // For a request matching "/users/{id}", returns {"id": "123"}
     *   Map<String, String> vars = helper.getUriTemplateVariables(serverWebRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} containing handler mapping attributes
     * @return a map of URI template variable names to their values, or {@code null} if not available
     */
    @Override
    public Map<String, String> getUriTemplateVariables(NativeWebRequest request) {
        return REQUEST.getAttribute(request, URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    /**
     * Gets the matrix variables from the request attributes, as resolved by Spring WebFlux's
     * {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   // For a request like "/users;color=red;size=10"
     *   Map<String, MultiValueMap<String, String>> matrix = helper.getMatrixVariables(serverWebRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} containing handler mapping attributes
     * @return a map of path segments to their matrix variables, or {@code null} if not available
     */
    @Override
    public Map<String, MultiValueMap<String, String>> getMatrixVariables(NativeWebRequest request) {
        return REQUEST.getAttribute(request, MATRIX_VARIABLES_ATTRIBUTE);
    }

    /**
     * Gets the set of producible {@link MediaType media types} from the request attributes,
     * as resolved by Spring WebFlux's {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   Set<MediaType> mediaTypes = helper.getProducibleMediaTypes(serverWebRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} containing handler mapping attributes
     * @return the set of producible media types, or {@code null} if not available
     */
    @Override
    public Set<MediaType> getProducibleMediaTypes(NativeWebRequest request) {
        return REQUEST.getAttribute(request, PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
    }

    /**
     * Returns the {@link SpringWebType} indicating this helper is for Spring WebFlux.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebFluxHelper helper = new SpringWebFluxHelper();
     *   SpringWebType type = helper.getType(); // returns SpringWebType.WEB_FLUX
     * }</pre>
     *
     * @return {@link SpringWebType#WEB_FLUX}
     */
    @Override
    public SpringWebType getType() {
        return WEB_FLUX;
    }

    /**
     * Gets the underlying {@link ServerHttpResponse} from the given {@link NativeWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServerHttpResponse response = getServerHttpResponse(serverWebRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to extract the response from
     * @return the underlying {@link ServerHttpResponse}
     */
    protected ServerHttpResponse getServerHttpResponse(NativeWebRequest request) {
        ServerWebRequest serverWebRequest = getServerWebRequest(request);
        return serverWebRequest.getResponse();
    }

    /**
     * Gets the {@link HttpHeaders} from the response of the given {@link NativeWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   HttpHeaders headers = getResponseHeaders(serverWebRequest);
     *   headers.set("X-Custom-Header", "value");
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to extract response headers from
     * @return the response {@link HttpHeaders}
     */
    protected HttpHeaders getResponseHeaders(NativeWebRequest request) {
        ServerHttpResponse serverHttpResponse = getServerHttpResponse(request);
        return serverHttpResponse.getHeaders();
    }

    /**
     * Gets the underlying {@link ServerHttpRequest} from the given {@link NativeWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServerHttpRequest httpRequest = getServerHttpRequest(serverWebRequest);
     *   String path = httpRequest.getPath().value();
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to extract the request from
     * @return the underlying {@link ServerHttpRequest}
     */
    protected ServerHttpRequest getServerHttpRequest(NativeWebRequest request) {
        ServerWebRequest serverWebRequest = getServerWebRequest(request);
        return serverWebRequest.getRequest();
    }

    /**
     * Casts the given {@link NativeWebRequest} to a {@link ServerWebRequest}.
     * Throws an {@link IllegalArgumentException} if the request is not a {@link ServerWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServerWebRequest result = getServerWebRequest(serverWebRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to cast
     * @return the request cast to {@link ServerWebRequest}
     * @throws IllegalArgumentException if the request is not a {@link ServerWebRequest}
     */
    protected ServerWebRequest getServerWebRequest(NativeWebRequest request) {
        if (request instanceof ServerWebRequest) {
            return (ServerWebRequest) request;
        }
        throw new IllegalArgumentException("The NativeWebRequest is not a ServerWebRequest");
    }
}
