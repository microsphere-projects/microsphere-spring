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

package io.microsphere.spring.webmvc.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.web.util.SpringWebHelper;
import io.microsphere.spring.web.util.SpringWebType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.microsphere.spring.web.util.SpringWebType.WEB_MVC;
import static io.microsphere.spring.web.util.WebRequestUtils.METHOD_HEADER_NAME;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static io.microsphere.util.Assert.assertTrue;
import static java.util.stream.Stream.of;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

/**
 * {@link SpringWebHelper} for Spring WebMVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringWebHelper
 * @since 1.0.0
 */
public class SpringWebMvcHelper implements SpringWebHelper {

    /**
     * Gets the HTTP method from the given {@link NativeWebRequest}. First checks for a method
     * override header, then falls back to the actual servlet request method.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(httpServletRequest, httpServletResponse);
     *   String method = helper.getMethod(webRequest); // e.g. "GET"
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to extract the HTTP method from
     * @return the HTTP method string (e.g. "GET", "POST")
     */
    @Override
    public String getMethod(NativeWebRequest request) {
        String method = request.getHeader(METHOD_HEADER_NAME);
        if (method == null) {
            HttpServletRequest httpServletRequest = getHttpServletRequest(request);
            method = httpServletRequest.getMethod();
        }
        return method;
    }

    /**
     * Sets a response header on the underlying {@link HttpServletResponse}.
     * If the header already exists, its value is replaced.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   helper.setHeader(webRequest, "Content-Type", "application/json");
     * }</pre>
     *
     * @param request     the {@link NativeWebRequest} wrapping the servlet response
     * @param headerName  the name of the header to set
     * @param headerValue the value of the header to set
     */
    @Override
    public void setHeader(NativeWebRequest request, String headerName, String headerValue) {
        HttpServletResponse httpServletResponse = getHttpServletResponse(request);
        httpServletResponse.setHeader(headerName, headerValue);
    }

    /**
     * Adds one or more values to a response header on the underlying {@link HttpServletResponse}.
     * Unlike {@link #setHeader}, this appends values rather than replacing existing ones.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   helper.addHeader(webRequest, "Accept", "text/html", "application/json");
     * }</pre>
     *
     * @param request      the {@link NativeWebRequest} wrapping the servlet response
     * @param headerName   the name of the header to add values to
     * @param headerValues the values to add to the header
     */
    @Override
    public void addHeader(NativeWebRequest request, String headerName, String... headerValues) {
        HttpServletResponse httpServletResponse = getHttpServletResponse(request);
        for (String headerValue : headerValues) {
            httpServletResponse.addHeader(headerName, headerValue);
        }
    }

    /**
     * Gets the value of a cookie with the specified name from the underlying {@link HttpServletRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   String sessionId = helper.getCookieValue(webRequest, "JSESSIONID");
     * }</pre>
     *
     * @param request    the {@link NativeWebRequest} wrapping the servlet request
     * @param cookieName the name of the cookie to retrieve
     * @return the cookie value, or {@code null} if the cookie is not found
     */
    @Override
    public String getCookieValue(NativeWebRequest request, String cookieName) {
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies == null) {
            return null;
        }
        return of(cookies)
                .filter(Objects::nonNull)
                .filter(cookie -> Objects.equals(cookieName, cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    /**
     * Adds a cookie with the specified name and value to the underlying {@link HttpServletResponse}.
     * The cookie is created with {@code secure} and {@code httpOnly} flags enabled.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   helper.addCookie(webRequest, "session", "abc123");
     * }</pre>
     *
     * @param request     the {@link NativeWebRequest} wrapping the servlet response
     * @param cookieName  the name of the cookie to add
     * @param cookieValue the value of the cookie to add
     */
    @Override
    public void addCookie(NativeWebRequest request, String cookieName, String cookieValue) {
        HttpServletResponse response = getHttpServletResponse(request);
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * Gets the best matching handler from the request attributes, as resolved by Spring MVC's
     * {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   Object handler = helper.getBestMatchingHandler(webRequest);
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
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   String path = helper.getPathWithinHandlerMapping(webRequest); // e.g. "/users/123"
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
     * Gets the best matching URL pattern from the request attributes, as resolved by Spring MVC's
     * {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   String pattern = helper.getBestMatchingPattern(webRequest); // e.g. "/users/{id}"
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} containing handler mapping attributes
     * @return the best matching URL pattern, or {@code null} if not available
     */
    @Override
    public String getBestMatchingPattern(NativeWebRequest request) {
        return REQUEST.getAttribute(request, BEST_MATCHING_PATTERN_ATTRIBUTE);
    }

    /**
     * Gets the URI template variables from the request attributes, as resolved by Spring MVC's
     * {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   // For a request matching "/users/{id}", returns {"id": "123"}
     *   Map<String, String> vars = helper.getUriTemplateVariables(webRequest);
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
     * Gets the matrix variables from the request attributes, as resolved by Spring MVC's
     * {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   // For a request like "/users;color=red;size=10"
     *   Map<String, MultiValueMap<String, String>> matrix = helper.getMatrixVariables(webRequest);
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
     * as resolved by Spring MVC's {@code HandlerMapping}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   Set<MediaType> mediaTypes = helper.getProducibleMediaTypes(webRequest);
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
     * Returns the {@link SpringWebType} indicating this helper is for Spring Web MVC.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringWebMvcHelper helper = new SpringWebMvcHelper();
     *   SpringWebType type = helper.getType(); // returns SpringWebType.WEB_MVC
     * }</pre>
     *
     * @return {@link SpringWebType#WEB_MVC}
     */
    @Override
    public SpringWebType getType() {
        return WEB_MVC;
    }

    /**
     * Gets the underlying {@link HttpServletResponse} from the given {@link NativeWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   HttpServletResponse servletResponse = getHttpServletResponse(webRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to extract the response from
     * @return the underlying {@link HttpServletResponse}, never {@code null}
     */
    @Nonnull
    protected HttpServletResponse getHttpServletResponse(NativeWebRequest request) {
        ServletWebRequest servletWebRequest = getServletWebRequest(request);
        return servletWebRequest.getResponse();
    }

    /**
     * Gets the underlying {@link HttpServletRequest} from the given {@link NativeWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   HttpServletRequest servletRequest = getHttpServletRequest(webRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to extract the request from
     * @return the underlying {@link HttpServletRequest}, never {@code null}
     */
    @Nonnull
    protected HttpServletRequest getHttpServletRequest(NativeWebRequest request) {
        ServletWebRequest servletWebRequest = getServletWebRequest(request);
        return servletWebRequest.getRequest();
    }

    /**
     * Casts the given {@link NativeWebRequest} to a {@link ServletWebRequest}.
     * Throws an assertion error if the request is not a {@link ServletWebRequest}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServletWebRequest webRequest = new ServletWebRequest(request, response);
     *   ServletWebRequest result = getServletWebRequest(webRequest);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} to cast
     * @return the request cast to {@link ServletWebRequest}
     */
    protected ServletWebRequest getServletWebRequest(NativeWebRequest request) {
        assertTrue(request instanceof ServletWebRequest, () -> "The NativeWebRequest is not a ServletWebRequest");
        return (ServletWebRequest) request;
    }
}