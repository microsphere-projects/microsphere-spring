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


import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
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

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.web.util.SpringWebType.WEB_MVC;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        TestController.class,
        SpringWebMvcHelper.class,
        SpringWebMvcHelperTest.class
})
@EnableWebMvc
@RestControllerAdvice(assignableTypes = TestController.class)
public class SpringWebMvcHelperTest implements RequestBodyAdvice {

    private static final String PROCESSED_LISTENERS_ATTRIBUTE_NAME = "processedListeners";

    @Autowired
    private ConfigurableWebApplicationContext wac;

    @Autowired
    private SpringWebMvcHelper springWebMvcHelper;

    @Autowired
    private TestController testController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(wac).build();
    }

    @Test
    public void testGreeting() throws Exception {
        HttpMethod method = GET;
        String uriTemplate = "/test/greeting/{message}";
        String[] uriVariables = ofArray("message", "Mercy");
        Cookie cookie = new Cookie("JSESSIONID", "123456");
        Cookie[] cookies = ofArray(cookie);
        String[] headerNames = ofArray(ACCEPT, CONTENT_TYPE);
        String[] headerValues = ofArray(APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE);
        ApplicationListener<RequestHandledEvent> listener = event -> {
            if (isProcessed(this)) {
                return;
            }
            ServletWebRequest request = getServletWebRequest();
            testGetMethod(request, method);
            testGetCookieValue(request, cookies);
            testGetBestMatchingHandler(request, "greeting", String.class);
            testGetPathWithinHandlerMapping(request, uriTemplate, uriVariables);
            testGetBestMatchingPattern(request, uriTemplate);
            testGetUriTemplateVariables(request, uriVariables);
            testGetMatrixVariables(request);
            testSetHeader(request, headerNames[0], headerValues[0]);
            testAddHeader(request, headerNames[1], headerValues[1]);
            testAddCookie(request, cookie.getName(), cookie.getValue());
            process(this);
        };
        this.wac.addApplicationListener(listener);

        this.mockMvc.perform(
                        request(method, uriTemplate, uriVariables[1])
                                .cookie(cookies)
                                .characterEncoding("UTF_8")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(headerNames[0], headerValues[0]))
                .andExpect(header().string(headerNames[1], headerValues[1] + ";charset=ISO-8859-1"))
                .andExpect(cookie().value(cookie.getName(), cookie.getValue()))
                .andExpect(content().string(testController.greeting(uriVariables[1])));

        // this.wac.removeApplicationListener(listener);
    }

    @Test
    public void testUser() throws Exception {
        HttpMethod method = POST;
        String uriTemplate = "/test/user";
        MediaType contentType = APPLICATION_JSON;
        Cookie cookie = new Cookie("JSESSIONID", "123456");
        Cookie[] cookies = ofArray(cookie);
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);

        ApplicationListener<RequestHandledEvent> listener = event -> {
            if (isProcessed(this)) {
                return;
            }
            ServletWebRequest request = getServletWebRequest();
            testGetMethod(request, method);
            testGetCookieValue(request, cookies);
            testGetBestMatchingHandler(request, "user", User.class);
            testGetPathWithinHandlerMapping(request, uriTemplate);
            testGetBestMatchingPattern(request, uriTemplate);
            testGetUriTemplateVariables(request);
            testGetMatrixVariables(request);
            testGetProducibleMediaTypes(request, contentType);
            testGetRequestBody(request, user);
            process(this);
        };

        this.wac.addApplicationListener(listener);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(request(method, uriTemplate)
                        .cookie(cookies)
                        .contentType(contentType)
                        .content(json))
                .andExpect(status().isOk());

        // this.wac.removeApplicationListener(listener);
    }

    @Test
    public void testGetType() {
        assertSame(WEB_MVC, springWebMvcHelper.getType());
    }

    boolean isProcessed(Object listener) {
        ServletContext servletContext = getServletContext();
        Set<Object> processedListeners = (Set<Object>) servletContext.getAttribute(PROCESSED_LISTENERS_ATTRIBUTE_NAME);
        return processedListeners != null && processedListeners.contains(listener);
    }

    void process(Object listener) {
        ServletContext servletContext = getServletContext();
        Set<Object> processedListeners = (Set<Object>) servletContext.getAttribute(PROCESSED_LISTENERS_ATTRIBUTE_NAME);
        if (processedListeners == null) {
            processedListeners = new HashSet<>();
            servletContext.setAttribute(PROCESSED_LISTENERS_ATTRIBUTE_NAME, processedListeners);
        }
        processedListeners.add(listener);
    }

    ServletContext getServletContext() {
        return this.wac.getServletContext();
    }

    ServletWebRequest getServletWebRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) getRequestAttributes();
        return new ServletWebRequest(requestAttributes.getRequest(), requestAttributes.getResponse());
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
        URI uri = fromPath(uriTemplate).buildAndExpand(uriVariablesMap).toUri();
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
        if (matrixVariables == null) {
            matrixVariables = emptyMap();
        }
        Map<String, String> variablesMap = ofMap(variables);
        assertEquals(variablesMap, matrixVariables);
    }

    void testSetHeader(ServletWebRequest request, String headerName, String headerValue) {
        this.springWebMvcHelper.setHeader(request, headerName, headerValue);
    }

    void testAddHeader(ServletWebRequest request, String headerName, String headerValue) {
        this.springWebMvcHelper.addHeader(request, headerName, headerValue);
    }

    void testAddCookie(ServletWebRequest request, String name, String value) {
        this.springWebMvcHelper.addCookie(request, name, value);
    }

    void testGetRequestBody(NativeWebRequest request, Object expectedRequestBody) {
        Object requestBody = this.springWebMvcHelper.getRequestBody(request, expectedRequestBody.getClass());
        assertEquals(expectedRequestBody, requestBody);
    }

    void testGetProducibleMediaTypes(NativeWebRequest request, MediaType... mediaTypes) {
        Set<MediaType> producibleMediaTypes = this.springWebMvcHelper.getProducibleMediaTypes(request);
        if (producibleMediaTypes == null) {
            producibleMediaTypes = emptySet();
        }
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