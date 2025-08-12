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

package io.microsphere.spring.webflux.context.request;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.spring.webflux.util.AttributeScope;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import java.security.Principal;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.web.util.WebUtils.SESSION_MUTEX_ATTRIBUTE;

/**
 * The adapter implementation of {@link NativeWebRequest} based on {@link ServerWebExchange}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServerWebExchange
 * @see ServerHttpRequest
 * @see ServerHttpResponse
 * @see NativeWebRequest
 * @since 1.0.0
 */
public class NativeWebRequestAdapter implements NativeWebRequest {

    private static final Logger logger = getLogger(NativeWebRequestAdapter.class);

    private final ServerWebExchange exchange;

    private final ServerHttpRequest request;

    public NativeWebRequestAdapter(ServerWebExchange exchange) {
        this.exchange = exchange;
        this.request = exchange.getRequest();
    }

    @Override
    public Object getNativeRequest() {
        return request;
    }

    @Override
    public Object getNativeResponse() {
        return exchange.getResponse();
    }

    @Override
    public <T> T getNativeRequest(Class<T> requiredType) {
        Object nativeRequest = getNativeRequest();
        if (isAssignableFrom(requiredType, nativeRequest.getClass())) {
            return (T) nativeRequest;
        }
        return null;
    }

    @Override
    public <T> T getNativeResponse(Class<T> requiredType) {
        Object nativeResponse = getNativeResponse();
        if (isAssignableFrom(requiredType, nativeResponse.getClass())) {
            return (T) nativeResponse;
        }
        return null;
    }

    @Override
    public String getHeader(String headerName) {
        HttpHeaders httpHeaders = getHttpHeaders();
        return httpHeaders.getFirst(headerName);
    }

    @Override
    public String[] getHeaderValues(String headerName) {
        HttpHeaders httpHeaders = getHttpHeaders();
        List<String> headerValues = httpHeaders.get(headerName);
        return toArray(headerValues);
    }

    @Override
    public Iterator<String> getHeaderNames() {
        HttpHeaders httpHeaders = getHttpHeaders();
        return httpHeaders.keySet().iterator();
    }

    @Override
    public String getParameter(String paramName) {
        MultiValueMap<String, String> queryParams = getQueryParams();
        return queryParams.getFirst(paramName);
    }

    @Override
    public String[] getParameterValues(String paramName) {
        MultiValueMap<String, String> queryParams = getQueryParams();
        List<String> paramValues = queryParams.get(paramName);
        return toArray(paramValues);
    }

    @Override
    public Iterator<String> getParameterNames() {
        MultiValueMap<String, String> queryParams = getQueryParams();
        return queryParams.keySet().iterator();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        MultiValueMap<String, String> queryParams = getQueryParams();
        Map<String, String[]> parameterMap = newFixedLinkedHashMap(queryParams.size());
        for (Entry<String, List<String>> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            String[] valueArray = toArray(values);
            parameterMap.put(key, valueArray);
        }
        return unmodifiableMap(parameterMap);
    }

    @Override
    public Locale getLocale() {
        LocaleContext localeContext = this.exchange.getLocaleContext();
        return localeContext.getLocale();
    }

    @Override
    public String getContextPath() {
        return this.request.getPath().contextPath().value();
    }

    @Override
    public String getRemoteUser() {
        return "";
    }

    @Override
    public Principal getUserPrincipal() {
        return this.exchange.getPrincipal().block();
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean checkNotModified(long lastModifiedTimestamp) {
        Instant lastModified = ofEpochMilli(lastModifiedTimestamp);
        return this.exchange.checkNotModified(lastModified);
    }

    @Override
    public boolean checkNotModified(String etag) {
        return this.exchange.checkNotModified(etag);
    }

    @Override
    public boolean checkNotModified(String etag, long lastModifiedTimestamp) {
        Instant lastModified = ofEpochMilli(lastModifiedTimestamp);
        return this.exchange.checkNotModified(etag, lastModified);
    }

    @Override
    public String getDescription(boolean includeClientInfo) {
        return "";
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return AttributeScope.getAttribute(this.exchange, name, scope);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        AttributeScope.setAttribute(this.exchange, name, value, scope);
    }

    @Override
    public void removeAttribute(String name, int scope) {
        AttributeScope.removeAttribute(this.exchange, name, scope);
    }

    @Override
    public String[] getAttributeNames(int scope) {
        return AttributeScope.getAttributeNames(this.exchange, scope);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        logger.warn("The method 'registerDestructionCallback' is not supported in WebFlux, " +
                        "the callback will not be registered for the attribute[name : '{}' , callback : {} , scope : {}]",
                name, callback, scope);
    }

    @Override
    public Object resolveReference(String key) {
        switch (key) {
            case "request":
                return this.getNativeRequest();
            case "response":
                return this.getNativeResponse();
            case "session":
                return this.getSession();
        }
        return null;
    }

    @Override
    public String getSessionId() {
        WebSession webSession = this.getSession();
        return webSession.getId();
    }

    @Override
    public Object getSessionMutex() {
        WebSession session = this.getSession();
        Object mutex = session.getAttribute(SESSION_MUTEX_ATTRIBUTE);
        if (mutex == null) {
            mutex = session;
        }
        return mutex;
    }

    @Nonnull
    protected HttpHeaders getHttpHeaders() {
        return this.request.getHeaders();
    }

    @Nonnull
    protected MultiValueMap<String, String> getQueryParams() {
        return this.request.getQueryParams();
    }

    protected WebSession getSession() {
        return this.exchange.getSession().block();
    }

    protected String[] toArray(List<String> values) {
        return values == null ? null : values.toArray(new String[0]);
    }
}
