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


import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Map;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static io.microsphere.spring.web.util.WebSource.MATRIX_VARIABLE;
import static io.microsphere.spring.web.util.WebSource.PATH_VARIABLE;
import static io.microsphere.spring.web.util.WebSource.REQUEST_ATTRIBUTE;
import static io.microsphere.spring.web.util.WebSource.REQUEST_BODY;
import static io.microsphere.spring.web.util.WebSource.REQUEST_COOKIE;
import static io.microsphere.spring.web.util.WebSource.REQUEST_HEADER;
import static io.microsphere.spring.web.util.WebSource.REQUEST_PARAMETER;
import static io.microsphere.spring.web.util.WebSource.SESSION_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

/**
 * {@link WebSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebSource
 * @since 1.0.0
 */
class WebSourceTest {

    static final String testName = "test";

    static final String testValue = "value";

    @Test
    void testGetValueForATTRIBUTE() {
        NativeWebRequest request = createWebRequest(r -> r.setAttribute(testName, testValue));
        assertEquals(testValue, REQUEST_ATTRIBUTE.getValue(request, testName));
    }

    @Test
    void testGetValueForSESSION_ATTRIBUTE() {
        NativeWebRequest request = createWebRequest(r -> r.getSession().setAttribute(testName, testValue));
        assertEquals(testValue, SESSION_ATTRIBUTE.getValue(request, testName));
    }


    @Test
    void testGetValueForPARAMETER() {
        NativeWebRequest request = createWebRequest(r -> r.setParameter(testName, testValue));
        assertEquals(testValue, REQUEST_PARAMETER.getValue(request, testName));
    }

    @Test
    void testGetValueForHEADER() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(testName, testValue));
        assertEquals(testValue, REQUEST_HEADER.getValue(request, testName));
    }

    @Test
    void testGetValueForCOOKIE() {
        NativeWebRequest request = createWebRequest(r -> r.setCookies(new Cookie(testName, testValue)));
        assertEquals(testValue, REQUEST_COOKIE.getValue(request, testName));
    }

    @Test
    void testGetValueForCOOKIEOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(REQUEST_COOKIE.getValue(request, testName));
    }

    @Test
    void testGetValueForBODY() {
        Map<String, Object> body = ofMap(testName, testValue);
        Method method = findMethod(getClass(), "testGetValueForBODY");
        NativeWebRequest request = createWebRequest(r -> {
            HandlerMethod handlerMethod = new HandlerMethod(this, method);
            r.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, handlerMethod);
        });
        setHandlerMethodRequestBodyArgument(request, method, body);
        assertEquals(testValue, REQUEST_BODY.getValue(request, testName));
    }

    @Test
    void testGetValueForBODYOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(REQUEST_BODY.getValue(request, testName));
    }

    @Test
    void testGetValueForPATH_VARIABLE() {
        Map<String, String> uriTemplateVariables = ofMap(testName, testValue);
        NativeWebRequest request = createWebRequest(r -> {
            r.setAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVariables);
        });
        assertEquals(testValue, PATH_VARIABLE.getValue(request, testName));
    }

    @Test
    void testGetValueForPATH_VARIABLEOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(PATH_VARIABLE.getValue(request, testName));
    }

    @Test
    void testGetValueForMATRIX_VARIABLE() {
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        httpHeaders.add("h1", "v1");
        Map<String, MultiValueMap<String, String>> matrixVariables = ofMap(testName, httpHeaders);
        NativeWebRequest request = createWebRequest(r -> {
            r.setAttribute(MATRIX_VARIABLES_ATTRIBUTE, matrixVariables);
        });
        assertEquals("v1", MATRIX_VARIABLE.getValue(request, "h1"));
    }

    @Test
    void testGetValueForMATRIX_VARIABLEOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(MATRIX_VARIABLE.getValue(request, testName));

        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        Map<String, MultiValueMap<String, String>> matrixVariables = ofMap(testName, httpHeaders);
        REQUEST.setAttribute(request, MATRIX_VARIABLES_ATTRIBUTE, matrixVariables);
        assertNull(MATRIX_VARIABLE.getValue(request, testName));

        httpHeaders.add("h1", "v1");
        assertNull(MATRIX_VARIABLE.getValue(request, "h2"));
    }
}