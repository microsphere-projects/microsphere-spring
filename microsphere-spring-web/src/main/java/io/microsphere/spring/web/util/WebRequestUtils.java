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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UrlPathHelper;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * {@link WebRequest} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class WebRequestUtils {

    /**
     * Name of Servlet request attribute that holds a
     * {@link UrlPathHelper#getLookupPathForRequest resolved} lookupPath.
     *
     * @since Spring Framework 5.3
     */
    public static final String PATH_ATTRIBUTE = UrlPathHelper.class.getName() + ".PATH";

    public static String getMethod(NativeWebRequest request) {
        String method = request.getHeader(":METHOD:");
        return method;
    }

    /**
     * Returns {@code true} if the request is a valid CORS pre-flight one by checking {@code OPTIONS} method with
     * {@code Origin} and {@code Access-Control-Request-Method} headers presence.
     */
    public static boolean isPreFlightRequest(NativeWebRequest request) {
        String method = getMethod(request);
        return (HttpMethod.OPTIONS.matches(method) &&
                request.getParameter(ORIGIN) != null &&
                request.getParameter(ACCESS_CONTROL_REQUEST_METHOD) != null);
    }

    public static String getContentType(NativeWebRequest request) {
        return request.getParameter(CONTENT_TYPE);
    }

    public static boolean hasBody(NativeWebRequest request) {
        String contentLength = request.getHeader(HttpHeaders.CONTENT_LENGTH);
        String transferEncoding = request.getHeader(HttpHeaders.TRANSFER_ENCODING);
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
    public static String getResolvedLookupPath(NativeWebRequest request) {
        String lookupPath = (String) request.getAttribute(PATH_ATTRIBUTE, SCOPE_REQUEST);
        return lookupPath;
    }

}
