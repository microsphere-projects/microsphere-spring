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

package io.microsphere.spring.test.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.test.web.servlet.TestServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Consumer;

import static io.microsphere.collection.MapUtils.of;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * Web Utilities class for Spring Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NativeWebRequest
 * @see MockHttpServletRequest
 * @since 1.0.0
 */
public abstract class SpringTestWebUtils {

    public static final String PATH_ATTRIBUTE = UrlPathHelper.class.getName() + ".PATH";

    /**
     * Creates a new instance of {@link NativeWebRequest} with default settings.
     * <p>
     * This method is equivalent to calling {@link #createWebRequest(Consumer)} with an empty consumer.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = SpringTestWebUtils.createWebRequest();
     * }</pre>
     *
     * @return a new instance of {@link NativeWebRequest}
     * @see #createWebRequest(Consumer)
     */
    public static NativeWebRequest createWebRequest() {
        return createWebRequest(r -> {
        });
    }

    /**
     * Creates a new instance of {@link NativeWebRequest} with custom settings provided by the given {@link Consumer}.
     * <p>
     * This method allows for flexible configuration of the underlying {@link MockHttpServletRequest}
     * before creating the {@link ServletWebRequest}. The consumer can modify any aspect of the request,
     * such as headers, parameters, method, URI, etc.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = SpringTestWebUtils.createWebRequest(req -> {
     *     req.setMethod("POST");
     *     req.setRequestURI("/api/users");
     *     req.addHeader("Content-Type", "application/json");
     *     req.setParameter("id", "123");
     * });
     * }</pre>
     *
     * @param requestBuilder a {@link Consumer} that accepts and configures a {@link MockHttpServletRequest}
     * @return a new instance of {@link NativeWebRequest} based on the configured request
     * @see MockHttpServletRequest
     * @see ServletWebRequest
     */
    public static NativeWebRequest createWebRequest(Consumer<MockHttpServletRequest> requestBuilder) {
        MockHttpServletRequest request = new MockHttpServletRequest(new TestServletContext());
        MockHttpServletResponse response = new MockHttpServletResponse();
        requestBuilder.accept(request);
        return new ServletWebRequest(request, response);
    }

    /**
     * Creates a new instance of {@link NativeWebRequest} with the specified request URI.
     * <p>
     * This method configures the underlying {@link MockHttpServletRequest} with the given URI
     * and sets the {@link #PATH_ATTRIBUTE} to the same value. It's a convenient way to create
     * a web request for a specific endpoint.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = SpringTestWebUtils.createWebRequest("/api/users/123");
     * }</pre>
     *
     * @param requestURI the request URI to set on the {@link MockHttpServletRequest}
     * @return a new instance of {@link NativeWebRequest} configured with the given URI
     * @see #createWebRequest(Consumer)
     * @see MockHttpServletRequest#setRequestURI(String)
     * @see #PATH_ATTRIBUTE
     */
    public static NativeWebRequest createWebRequest(String requestURI) {
        return createWebRequest(request -> {
            request.setRequestURI(requestURI);
            request.setAttribute(PATH_ATTRIBUTE, requestURI);
        });
    }

    /**
     * Creates a new instance of {@link NativeWebRequest} with the specified request parameters.
     * <p>
     * This method configures the underlying {@link MockHttpServletRequest} with the given parameters
     * using {@link io.microsphere.collection.MapUtils#of(Object...)}. The parameters are passed as
     * key-value pairs in the form of {@code key1, value1, key2, value2, ...}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = SpringTestWebUtils.createWebRequestWithParams(
     *     "name", "John Doe",
     *     "age", "30"
     * );
     * }</pre>
     *
     * @param params the request parameters as key-value pairs
     * @return a new instance of {@link NativeWebRequest} configured with the given parameters
     * @see #createWebRequest(Consumer)
     * @see io.microsphere.collection.MapUtils#of(Object...)
     */
    public static NativeWebRequest createWebRequestWithParams(Object... params) {
        return createWebRequest(request -> {
            request.setParameters(of(params));
        });
    }

    /**
     * Creates a new instance of {@link NativeWebRequest} with the specified request headers.
     * <p>
     * This method configures the underlying {@link MockHttpServletRequest} with the given headers
     * using {@link io.microsphere.collection.MapUtils#of(Object...)}. The headers are passed as
     * key-value pairs in the form of {@code key1, value1, key2, value2, ...}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = SpringTestWebUtils.createWebRequestWithHeaders(
     *     "Content-Type", "application/json",
     *     "Authorization", "Bearer token123"
     * );
     * }</pre>
     *
     * @param headers the request headers as key-value pairs
     * @return a new instance of {@link NativeWebRequest} configured with the given headers
     * @see #createWebRequest(Consumer)
     * @see io.microsphere.collection.MapUtils#of(Object...)
     */
    public static NativeWebRequest createWebRequestWithHeaders(Object... headers) {
        return createWebRequestWithHeaders(of(headers));
    }

    /**
     * Creates a new instance of {@link NativeWebRequest} with the specified request headers.
     * <p>
     * This method configures the underlying {@link MockHttpServletRequest} with the given headers
     * using a {@link Map}. The headers are passed as a map of key-value pairs.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<String, String> headers = new HashMap<>();
     * headers.put("Content-Type", "application/json");
     * headers.put("Authorization", "Bearer token123");
     * NativeWebRequest request = SpringTestWebUtils.createWebRequestWithHeaders(headers);
     * }</pre>
     *
     * @param headers the request headers as a map of key-value pairs
     * @return a new instance of {@link NativeWebRequest} configured with the given headers
     * @see #createWebRequest(Consumer)
     * @see MockHttpServletRequest#addHeader(String, Object)
     */
    public static NativeWebRequest createWebRequestWithHeaders(Map<String, String> headers) {
        return createWebRequest(request -> {
            headers.forEach(request::addHeader);
        });
    }

    /**
     * Creates a new instance of {@link NativeWebRequest} for CORS pre-flight requests.
     * <p>
     * This method configures the underlying {@link MockHttpServletRequest} with the OPTIONS method
     * and adds the following headers:
     * <ul>
     *   <li>:METHOD: - set to "OPTIONS"</li>
     *   <li>Origin - set to "*"</li>
     *   <li>Access-Control-Request-Method - set to "*"</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest preFlightRequest = SpringTestWebUtils.createPreFightRequest();
     * }</pre>
     *
     * @return a new instance of {@link NativeWebRequest} configured for CORS pre-flight requests
     * @see #createWebRequest(Consumer)
     * @see MockHttpServletRequest
     */
    public static NativeWebRequest createPreFightRequest() {
        // Create pre-flight request (OPTIONS method with Origin header)
        return createWebRequest(request -> {
            request.setMethod(OPTIONS.name());
            request.addHeader(":METHOD:", request.getMethod());
            request.addHeader(ORIGIN, "*");
            request.addHeader(ACCESS_CONTROL_REQUEST_METHOD, "*");
        });
    }

    /**
     * Clears all attributes from the request scope of the given {@link NativeWebRequest}.
     * <p>
     * This method removes all attributes currently set in the request scope. It's useful
     * for cleaning up request state between tests or operations.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = SpringTestWebUtils.createWebRequest();
     * request.setAttribute("key1", "value1", RequestAttributes.SCOPE_REQUEST);
     * request.setAttribute("key2", "value2", RequestAttributes.SCOPE_REQUEST);
     *
     * // Clear all request-scoped attributes
     * SpringTestWebUtils.clearAttributes(request);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} from which to clear attributes
     * @see #clearAttributes(NativeWebRequest, int)
     * @see org.springframework.web.context.request.RequestAttributes#SCOPE_REQUEST
     */
    public static void clearAttributes(@Nonnull NativeWebRequest request) {
        clearAttributes(request, SCOPE_REQUEST);
    }

    /**
     * Clears all attributes from the specified scope of the given {@link NativeWebRequest}.
     * <p>
     * This method removes all attributes currently set in the specified scope (either request or session).
     * It's useful for cleaning up request or session state between tests or operations.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * NativeWebRequest request = SpringTestWebUtils.createWebRequest();
     * request.setAttribute("key1", "value1", RequestAttributes.SCOPE_REQUEST);
     * request.setAttribute("key2", "value2", RequestAttributes.SCOPE_SESSION);
     *
     * // Clear all request-scoped attributes
     * SpringTestWebUtils.clearAttributes(request, RequestAttributes.SCOPE_REQUEST);
     *
     * // Clear all session-scoped attributes
     * SpringTestWebUtils.clearAttributes(request, RequestAttributes.SCOPE_SESSION);
     * }</pre>
     *
     * @param request the {@link NativeWebRequest} from which to clear attributes
     * @param scope the scope from which to clear attributes.
     *              Use {@link org.springframework.web.context.request.RequestAttributes#SCOPE_REQUEST SCOPE_REQUEST}
     *              or {@link org.springframework.web.context.request.RequestAttributes#SCOPE_SESSION SCOPE_SESSION}
     * @see #clearAttributes(NativeWebRequest)
     * @see org.springframework.web.context.request.RequestAttributes#SCOPE_REQUEST
     * @see org.springframework.web.context.request.RequestAttributes#SCOPE_SESSION
     */
    public static void clearAttributes(@Nonnull NativeWebRequest request, int scope) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        switch (scope) {
            case SCOPE_REQUEST:
                clearAttributes(servletRequest.getAttributeNames(), servletRequest::removeAttribute);
                break;
            case SCOPE_SESSION:
                HttpSession session = servletRequest.getSession(false);
                if (session != null) {
                    clearAttributes(session.getAttributeNames(), session::removeAttribute);
                }
                break;
        }
    }

    static void clearAttributes(@Nonnull Enumeration<String> attributeNames, @Nonnull Consumer<String> attributeToRemove) {
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            attributeToRemove.accept(attributeName);
        }
    }

    private SpringTestWebUtils() {
    }
}