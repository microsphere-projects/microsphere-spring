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


import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.webflux.annotation.AbstractEnableWebFluxExtensionTest;
import io.microsphere.spring.webflux.annotation.EnableWebFluxExtension;
import io.microsphere.spring.webflux.context.request.ServerWebRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.WebFilter;

import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.web.util.SpringWebType.WEB_FLUX;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

/**
 * {@link SpringWebFluxHelper} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringWebFluxHelper
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        TestController.class,
        SpringWebFluxHelper.class,
        SpringWebFluxHelperTest.class
})
@EnableWebFluxExtension(storeRequestBodyArgument = true)
class SpringWebFluxHelperTest extends AbstractEnableWebFluxExtensionTest {

    @Autowired
    private SpringWebFluxHelper springWebFluxHelper;

    @Override
    protected void testGreeting() {
        HttpMethod method = GET;
        String uriTemplate = "/test/greeting/{message}";
        String message = "Mercy";
        String[] uriVariables = ofArray("message", message);
        HttpCookie cookie = new HttpCookie("JSESSIONID", "123456");
        HttpCookie[] cookies = ofArray(cookie);
        String[] headerNames = ofArray(ACCEPT, CONTENT_TYPE);
        String[] headerValues = ofArray(APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE);

        WebFilter webFilter = (exchange, chain) -> {
            NativeWebRequest request = new ServerWebRequest(exchange);
            testSetHeader(request, headerNames[0], headerValues[0]);
            testAddHeader(request, headerNames[1], headerValues[1]);
            testAddCookie(request, cookie.getName(), cookie.getValue());
            return chain.filter(exchange).doOnTerminate(() -> {
                testGetMethod(request, method);
                testGetCookieValue(request, cookies);
                testGetBestMatchingHandler(request, "greeting", String.class);
                testGetPathWithinHandlerMapping(request, uriTemplate, uriVariables);
                testGetBestMatchingPattern(request, uriTemplate);
                testGetUriTemplateVariables(request, uriVariables);
                testGetMatrixVariables(request);
                testGetProducibleMediaTypes(request);
            });
        };

        compositeWebFilter.addFilter(webFilter);

        this.webTestClient
                .method(method)
                .uri(uriTemplate, message)
                .cookie(cookie.getName(), cookie.getValue())
                .headers(httpHeaders -> {
                    httpHeaders.set(headerNames[0], headerValues[0]);
                    httpHeaders.add(headerNames[1], headerValues[1]);
                })
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(headerNames[0], headerValues[0])
                .expectHeader().valueEquals(headerNames[1], headerValues[1])
                .expectCookie().valueEquals(cookie.getName(), cookie.getValue())
                .expectBody(String.class).isEqualTo(testController.greeting(message));

        compositeWebFilter.removeFilter(webFilter);
    }

    @Override
    protected void testUser() {
        HttpMethod method = POST;
        String uriTemplate = "/test/user";
        MediaType contentType = APPLICATION_JSON;
        HttpCookie cookie = new HttpCookie("JSESSIONID", "123456");
        HttpCookie[] cookies = ofArray(cookie);
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);

        WebFilter webFilter = (exchange, chain) -> {
            NativeWebRequest request = new ServerWebRequest(exchange);
            testAddCookie(request, cookie.getName(), cookie.getValue());
            return chain.filter(exchange).doOnTerminate(() -> {
                testGetMethod(request, method);
                testGetCookieValue(request, cookies);
                testGetBestMatchingHandler(request, "user", User.class);
                testGetPathWithinHandlerMapping(request, uriTemplate);
                testGetBestMatchingPattern(request, uriTemplate);
                testGetUriTemplateVariables(request);
                testGetMatrixVariables(request);
                testGetProducibleMediaTypes(request, contentType);
                testGetRequestBody(request, user);
            });
        };

        compositeWebFilter.addFilter(webFilter);

        this.webTestClient
                .method(method)
                .uri(uriTemplate)
                .cookie(cookie.getName(), cookie.getValue())
                .accept(contentType)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().valueEquals(cookie.getName(), cookie.getValue())
                .expectBody(User.class).isEqualTo(testController.user(user));

        compositeWebFilter.removeFilter(webFilter);
    }

    @Test
    void testGetType() {
        assertSame(WEB_FLUX, springWebFluxHelper.getType());
    }

    void testGetMethod(NativeWebRequest request, HttpMethod httpMethod) {
        String method = this.springWebFluxHelper.getMethod(request);
        assertEquals(httpMethod.name(), method);
    }

    void testGetCookieValue(NativeWebRequest request, HttpCookie... cookies) {
        for (HttpCookie cookie : cookies) {
            String cookieValue = this.springWebFluxHelper.getCookieValue(request, cookie.getName());
            assertEquals(cookie.getValue(), cookieValue);
        }
    }

    void testGetBestMatchingHandler(NativeWebRequest request, String methodName, Class<?>... parameterTypes) {
        Object handler = this.springWebFluxHelper.getBestMatchingHandler(request);
        assertTrue(handler instanceof HandlerMethod);
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        assertSame(this.testController.getClass(), handlerMethod.getBeanType());
        assertEquals(methodName, method.getName());
        assertArrayEquals(parameterTypes, method.getParameterTypes());
    }

    void testGetPathWithinHandlerMapping(NativeWebRequest request, String uriTemplate, Object... uriVariables) {
        Map<String, String> uriVariablesMap = ofMap(uriVariables);
        URI uri = fromPath(uriTemplate).build(uriVariablesMap);
        String path = this.springWebFluxHelper.getPathWithinHandlerMapping(request);
        assertEquals(uri.getPath(), path);
    }

    void testGetBestMatchingPattern(NativeWebRequest request, String uriTemplate) {
        String bestMatchingPattern = this.springWebFluxHelper.getBestMatchingPattern(request);
        assertEquals(uriTemplate, bestMatchingPattern);
    }

    void testGetUriTemplateVariables(NativeWebRequest request, Object... uriVariables) {
        Map<String, String> uriTemplateVariables = this.springWebFluxHelper.getUriTemplateVariables(request);
        Map<String, String> uriVariablesMap = ofMap(uriVariables);
        assertEquals(uriVariablesMap, uriTemplateVariables);
    }

    void testGetMatrixVariables(NativeWebRequest request, Object... variables) {
        Map<String, MultiValueMap<String, String>> matrixVariables = this.springWebFluxHelper.getMatrixVariables(request);
        Map<String, String> variablesMap = ofMap(variables);
        assertEquals(variablesMap, matrixVariables);
    }

    void testSetHeader(NativeWebRequest request, String headerName, String headerValue) {
        this.springWebFluxHelper.setHeader(request, headerName, headerValue);
    }

    void testAddHeader(NativeWebRequest request, String headerName, String headerValue) {
        this.springWebFluxHelper.addHeader(request, headerName, headerValue);
    }

    void testAddCookie(NativeWebRequest request, String name, String value) {
        this.springWebFluxHelper.addCookie(request, name, value);
    }

    void testGetRequestBody(NativeWebRequest request, Object expectedRequestBody) {
        Object requestBody = this.springWebFluxHelper.getRequestBody(request, expectedRequestBody.getClass());
        assertEquals(expectedRequestBody, requestBody);
    }

    void testGetProducibleMediaTypes(NativeWebRequest request, MediaType... mediaTypes) {
        Set<MediaType> producibleMediaTypes = this.springWebFluxHelper.getProducibleMediaTypes(request);
        if (producibleMediaTypes == null) {
            producibleMediaTypes = emptySet();
        }
        assertEquals(ofSet(mediaTypes), producibleMediaTypes);
    }
}