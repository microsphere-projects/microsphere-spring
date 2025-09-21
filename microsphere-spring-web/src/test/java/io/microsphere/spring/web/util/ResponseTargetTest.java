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
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.util.RequestSourceTest.testName;
import static io.microsphere.spring.web.util.RequestSourceTest.testValue;
import static io.microsphere.spring.web.util.ResponseTarget.BODY;
import static io.microsphere.spring.web.util.ResponseTarget.COOKIE;
import static io.microsphere.spring.web.util.ResponseTarget.HEADER;
import static io.microsphere.spring.web.util.WebRequestUtils.addCookie;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ResponseTarget} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResponseTarget
 * @since 1.0.0
 */
class ResponseTargetTest {

    private NativeWebRequest request;

    @BeforeEach
    void setUp() {
        this.request = createWebRequest();
    }

    @Test
    void testWriteValueForBODY() {
        assertThrows(UnsupportedOperationException.class, () -> BODY.writeValue(request, testName, testValue));
    }

    @Test
    void testWriteValueForHEADER() {
        HEADER.writeValue(request, testName, testValue);
        HttpServletResponse response = request.getNativeResponse(HttpServletResponse.class);
        assertEquals(testValue, response.getHeader(testName));
    }

    @Test
    void testWriteValueForCOOKIE() {
        COOKIE.writeValue(request, testName, testValue);
        addCookie(request, testName, testValue);
        MockHttpServletResponse response = request.getNativeResponse(MockHttpServletResponse.class);
        Cookie[] cookies = response.getCookies();
        assertEquals(testValue, cookies[0].getValue());
    }
}