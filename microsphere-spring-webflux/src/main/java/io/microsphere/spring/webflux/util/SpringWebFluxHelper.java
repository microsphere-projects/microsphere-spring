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

    @Override
    public String getMethod(NativeWebRequest request) {
        ServerHttpRequest serverHttpRequest = getServerHttpRequest(request);
        return serverHttpRequest.getMethod().name();
    }

    @Override
    public void setHeader(NativeWebRequest request, String headerName, String headerValue) {
        HttpHeaders httpHeaders = getResponseHeaders(request);
        httpHeaders.set(headerName, headerValue);
    }

    @Override
    public void addHeader(NativeWebRequest request, String headerName, String... headerValues) {
        HttpHeaders httpHeaders = getResponseHeaders(request);
        for (String headerValue : headerValues) {
            httpHeaders.add(headerName, headerValue);
        }
    }

    @Override
    public String getCookieValue(NativeWebRequest request, String cookieName) {
        ServerHttpRequest serverHttpRequest = getServerHttpRequest(request);
        MultiValueMap<String, HttpCookie> cookies = serverHttpRequest.getCookies();
        HttpCookie cookie = cookies.getFirst(cookieName);
        return cookie == null ? null : cookie.getValue();
    }

    @Override
    public void addCookie(NativeWebRequest request, String cookieName, String cookieValue) {
        ServerHttpResponse serverHttpResponse = getServerHttpResponse(request);
        ResponseCookie cookie = ResponseCookie.from(cookieName, cookieValue)
                .secure(true)
                .httpOnly(true)
                .build();
        serverHttpResponse.addCookie(cookie);
    }

    @Override
    public Object getBestMatchingHandler(NativeWebRequest request) {
        return REQUEST.getAttribute(request, BEST_MATCHING_HANDLER_ATTRIBUTE);
    }

    @Override
    public String getPathWithinHandlerMapping(NativeWebRequest request) {
        return REQUEST.getAttribute(request, PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

    @Override
    public String getBestMatchingPattern(NativeWebRequest request) {
        PathPattern pathPattern = REQUEST.getAttribute(request, BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pathPattern.getPatternString();
    }

    @Override
    public Map<String, String> getUriTemplateVariables(NativeWebRequest request) {
        return REQUEST.getAttribute(request, URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    }

    @Override
    public Map<String, MultiValueMap<String, String>> getMatrixVariables(NativeWebRequest request) {
        return REQUEST.getAttribute(request, MATRIX_VARIABLES_ATTRIBUTE);
    }

    @Override
    public Set<MediaType> getProducibleMediaTypes(NativeWebRequest request) {
        return REQUEST.getAttribute(request, PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
    }

    @Override
    public SpringWebType getType() {
        return WEB_FLUX;
    }

    protected ServerHttpResponse getServerHttpResponse(NativeWebRequest request) {
        ServerWebRequest serverWebRequest = getServerWebRequest(request);
        return serverWebRequest.getResponse();
    }

    protected HttpHeaders getResponseHeaders(NativeWebRequest request) {
        ServerHttpResponse serverHttpResponse = getServerHttpResponse(request);
        return serverHttpResponse.getHeaders();
    }

    protected HttpHeaders getRequestHeaders(NativeWebRequest request) {
        ServerWebRequest serverWebRequest = getServerWebRequest(request);
        return serverWebRequest.getRequestHeaders();
    }

    protected ServerHttpRequest getServerHttpRequest(NativeWebRequest request) {
        ServerWebRequest serverWebRequest = getServerWebRequest(request);
        return serverWebRequest.getRequest();
    }

    protected ServerWebRequest getServerWebRequest(NativeWebRequest request) {
        if (request instanceof ServerWebRequest) {
            return (ServerWebRequest) request;
        }
        throw new IllegalArgumentException("The NativeWebRequest is not a ServerWebRequest");
    }
}
