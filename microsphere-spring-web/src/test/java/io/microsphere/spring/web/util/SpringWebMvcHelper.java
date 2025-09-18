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
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.microsphere.spring.web.util.SpringWebType.WEB_MVC;
import static io.microsphere.spring.web.util.WebRequestUtils.METHOD_HEADER_NAME;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
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

    @Override
    public String getMethod(NativeWebRequest request) {
        String method = request.getHeader(METHOD_HEADER_NAME);
        if (method == null) {
            HttpServletRequest httpServletRequest = getHttpServletRequest(request);
            method = httpServletRequest.getMethod();
        }
        return method;
    }

    @Override
    public String getCookieValue(NativeWebRequest request, String cookieName) {
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);
        Cookie[] cookies = httpServletRequest.getCookies();
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookieName, cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public <T> T getRequestBody(NativeWebRequest request, Class<T> requestBodyType) {
        HttpServletRequest httpServletRequest = getHttpServletRequest(request);
        try {
            ServletInputStream inputStream = httpServletRequest.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
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
        return REQUEST.getAttribute(request, BEST_MATCHING_PATTERN_ATTRIBUTE);
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
        return WEB_MVC;
    }

    @Nonnull
    HttpServletRequest getHttpServletRequest(NativeWebRequest request) {
        if (request instanceof ServletWebRequest) {
            return (HttpServletRequest) request.getNativeRequest();
        }
        throw new IllegalArgumentException("The NativeWebRequest is not a ServletWebRequest");
    }
}
