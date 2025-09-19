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


import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.web.util.SpringWebType.WEB_MVC;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

/**
 * {@link SpringWebMvcHelper} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringWebMvcHelper
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        TestController.class,
        SpringWebMvcHelper.class,
        SpringWebMvcHelperTest.class
})
@EnableWebMvc
@RestControllerAdvice(assignableTypes = TestController.class)
class SpringWebMvcHelperTest implements RequestBodyAdvice {

    @Autowired
    private ConfigurableWebApplicationContext wac;

    @Autowired
    private SpringWebMvcHelper springWebMvcHelper;

    @Autowired
    private TestController testController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(wac).build();
    }

    @Test
    void testPathPattern() throws Exception {
        HttpMethod method = GET;
        String uriTemplate = "/test/greeting/{message}";
        Object[] uriVariables = ofArray("message", "Mercy");
        Cookie cookie = new Cookie("JSESSIONID", "123456");
        Cookie[] cookies = ofArray(cookie);
        ApplicationListener<RequestHandledEvent> listener = event -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) getRequestAttributes();
            ServletWebRequest request = new ServletWebRequest(requestAttributes.getRequest());
            testGetMethod(request, method);
            testGetCookieValue(request, cookies);
            testGetBestMatchingHandler(request, "greeting", String.class);
            testGetPathWithinHandlerMapping(request, uriTemplate, uriVariables);
            testGetBestMatchingPattern(request, uriTemplate);
            testGetUriTemplateVariables(request, uriVariables);
            testGetMatrixVariables(request);
        };
        this.wac.addApplicationListener(listener);

        this.mockMvc.perform(
                request(method, uriTemplate, uriVariables[1])
                        .cookie(cookies)
        ).andExpect(status().isOk());

        this.wac.removeApplicationListener(listener);
    }

    @Test
    void testJSONContent() throws Exception {
        HttpMethod method = POST;
        String uriTemplate = "/test/user";
        MediaType contentType = APPLICATION_JSON;
        Cookie cookie = new Cookie("JSESSIONID", "123456");
        Cookie[] cookies = ofArray(cookie);
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);

        ApplicationListener<RequestHandledEvent> listener = event -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) getRequestAttributes();
            ServletWebRequest request = new ServletWebRequest(requestAttributes.getRequest());
            testGetMethod(request, method);
            testGetCookieValue(request, cookies);
            testGetBestMatchingHandler(request, "user", User.class);
            testGetPathWithinHandlerMapping(request, uriTemplate);
            testGetBestMatchingPattern(request, uriTemplate);
            testGetUriTemplateVariables(request);
            testGetMatrixVariables(request);
            testGetProducibleMediaTypes(request, contentType);
            testGetRequestBody(request, user);
        };

        this.wac.addApplicationListener(listener);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(request(method, uriTemplate)
                        .cookie(cookies)
                        .contentType(contentType)
                        .content(json))
                .andExpect(status().isOk());

        this.wac.removeApplicationListener(listener);
    }

    @Test
    void testGetType() {
        assertSame(WEB_MVC, springWebMvcHelper.getType());
    }

    void testGetMethod(ServletWebRequest request, HttpMethod httpMethod) {
        String method = this.springWebMvcHelper.getMethod(request);
        assertEquals(httpMethod.name(), method);
    }

    void testGetCookieValue(ServletWebRequest request, Cookie... cookies) {
        for (Cookie cookie : cookies) {
            String cookieValue = this.springWebMvcHelper.getCookieValue(request, cookie.getName());
            assertEquals(cookie.getValue(), cookieValue);
        }
    }

    void testGetBestMatchingHandler(NativeWebRequest request, String methodName, Class<?>... parameterTypes) {
        Object handler = this.springWebMvcHelper.getBestMatchingHandler(request);
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
        String path = this.springWebMvcHelper.getPathWithinHandlerMapping(request);
        assertEquals(uri.getPath(), path);
    }

    void testGetBestMatchingPattern(NativeWebRequest request, String uriTemplate) {
        String bestMatchingPattern = this.springWebMvcHelper.getBestMatchingPattern(request);
        assertEquals(uriTemplate, bestMatchingPattern);
    }

    void testGetUriTemplateVariables(NativeWebRequest request, Object... uriVariables) {
        Map<String, String> uriTemplateVariables = this.springWebMvcHelper.getUriTemplateVariables(request);
        Map<String, String> uriVariablesMap = ofMap(uriVariables);
        assertEquals(uriVariablesMap, uriTemplateVariables);
    }

    void testGetMatrixVariables(NativeWebRequest request, Object... variables) {
        Map<String, MultiValueMap<String, String>> matrixVariables = this.springWebMvcHelper.getMatrixVariables(request);
        Map<String, String> variablesMap = ofMap(variables);
        assertEquals(variablesMap, matrixVariables);
    }

    void testGetRequestBody(NativeWebRequest request, Object expectedRequestBody) {
        Object requestBody = this.springWebMvcHelper.getRequestBody(request, expectedRequestBody.getClass());
        assertEquals(expectedRequestBody, requestBody);
    }

    void testGetProducibleMediaTypes(NativeWebRequest request, MediaType... mediaTypes) {
        Set<MediaType> producibleMediaTypes = this.springWebMvcHelper.getProducibleMediaTypes(request);
        assertEquals(ofSet(mediaTypes), producibleMediaTypes);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        setHandlerMethodRequestBodyArgument(parameter.getMethod(), body);
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}