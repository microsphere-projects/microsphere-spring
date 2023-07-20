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
import org.springframework.http.HttpRequest;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ORIGIN;

/**
 * HTTP Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class HttpUtils {

    /**
     * Returns {@code true} if the request is a valid CORS pre-flight one by checking {@code OPTIONS} method with
     * {@code Origin} and {@code Access-Control-Request-Method} headers presence.
     */
    public static boolean isPreFlightRequest(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return (HttpMethod.OPTIONS.equals(request.getMethod()) &&
                headers.containsKey(ORIGIN) &&
                headers.containsKey(ACCESS_CONTROL_REQUEST_METHOD));
    }

    public static String getContentType(HttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        return headers.getFirst(CONTENT_TYPE);
    }
}
