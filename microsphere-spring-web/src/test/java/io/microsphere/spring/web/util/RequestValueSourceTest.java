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


import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import java.lang.reflect.Method;
import java.util.Map;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.util.RequestAttributesUtils.setHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.web.util.RequestValueSource.ATTRIBUTE;
import static io.microsphere.spring.web.util.RequestValueSource.BODY;
import static io.microsphere.spring.web.util.RequestValueSource.COOKIE;
import static io.microsphere.spring.web.util.RequestValueSource.HEADER;
import static io.microsphere.spring.web.util.RequestValueSource.MATRIX_VARIABLE;
import static io.microsphere.spring.web.util.RequestValueSource.PARAMETER;
import static io.microsphere.spring.web.util.RequestValueSource.PATH_VARIABLE;
import static io.microsphere.spring.web.util.RequestValueSource.SESSION_ATTRIBUTE;
import static io.microsphere.spring.web.util.WebRequestUtils.PATH_ATTRIBUTE;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

/**
 * {@link RequestValueSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestValueSource
 * @since 1.0.0
 */
public class RequestValueSourceTest {

    private static final String testName = "test";

    private static final String testValue = "value";

    @Test
    public void testGetValueForATTRIBUTE() {
        NativeWebRequest request = createWebRequest(r -> r.setAttribute(PATH_ATTRIBUTE, SLASH));
        assertEquals(SLASH, ATTRIBUTE.getValue(request, PATH_ATTRIBUTE));
    }

    @Test
    public void testGetValueForSESSION_ATTRIBUTE() {
        NativeWebRequest request = createWebRequest(r -> r.getSession().setAttribute(PATH_ATTRIBUTE, SLASH));
        assertEquals(SLASH, SESSION_ATTRIBUTE.getValue(request, PATH_ATTRIBUTE));
    }

    @Test
    public void testGetValueForPARAMETER() {
        NativeWebRequest request = createWebRequest(r -> r.setParameter(testName, testValue));
        assertEquals(testValue, PARAMETER.getValue(request, testName));
    }

    @Test
    public void testGetValueForHEADER() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader(testName, testValue));
        assertEquals(testValue, HEADER.getValue(request, testName));
    }

    @Test
    public void testGetValueForCOOKIE() {
        NativeWebRequest request = createWebRequest(r -> r.setCookies(new Cookie(testName, testValue)));
        assertEquals(testValue, COOKIE.getValue(request, testName));
    }

    @Test
    public void testGetValueForCOOKIEOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(COOKIE.getValue(request, testName));
    }

    @Test
    public void testGetValueForBODY() {
        Map<String, Object> body = ofMap(testName, testValue);
        Method method = findMethod(getClass(), "testGetValueForBODY");
        NativeWebRequest request = createWebRequest(r -> {
            HandlerMethod handlerMethod = new HandlerMethod(this, method);
            r.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, handlerMethod);
        });
        setHandlerMethodRequestBodyArgument(request, method, body);
        assertEquals(testValue, BODY.getValue(request, testName));
    }

    @Test
    public void testGetValueForBODYOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(BODY.getValue(request, testName));
    }

    @Test
    public void testGetValueForPATH_VARIABLE() {
        Map<String, String> uriTemplateVariables = ofMap(testName, testValue);
        NativeWebRequest request = createWebRequest(r -> {
            r.setAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVariables);
        });
        assertEquals(testValue, PATH_VARIABLE.getValue(request, testName));
    }

    @Test
    public void testGetValueForPATH_VARIABLEOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(PATH_VARIABLE.getValue(request, testName));
    }

    @Test
    public void testGetValueForMATRIX_VARIABLE() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("h1", "v1");
        Map<String, MultiValueMap<String, String>> matrixVariables = ofMap(testName, httpHeaders);
        NativeWebRequest request = createWebRequest(r -> {
            r.setAttribute(MATRIX_VARIABLES_ATTRIBUTE, matrixVariables);
        });
        assertEquals("v1", MATRIX_VARIABLE.getValue(request, "h1"));
    }

    @Test
    public void testGetValueForMATRIX_VARIABLEOnNull() {
        NativeWebRequest request = createWebRequest();
        assertNull(MATRIX_VARIABLE.getValue(request, testName));

        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, MultiValueMap<String, String>> matrixVariables = ofMap(testName, httpHeaders);
        REQUEST.setAttribute(request, MATRIX_VARIABLES_ATTRIBUTE, matrixVariables);
        assertNull(MATRIX_VARIABLE.getValue(request, testName));

        httpHeaders.add("h1", "v1");
        assertNull(MATRIX_VARIABLE.getValue(request, "h2"));
    }
}