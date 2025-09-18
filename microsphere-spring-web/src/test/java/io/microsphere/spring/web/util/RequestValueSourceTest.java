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
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.Cookie;

import static io.microsphere.constants.PathConstants.SLASH;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.util.RequestValueSource.ATTRIBUTE;
import static io.microsphere.spring.web.util.RequestValueSource.COOKIE;
import static io.microsphere.spring.web.util.RequestValueSource.HEADER;
import static io.microsphere.spring.web.util.RequestValueSource.PARAMETER;
import static io.microsphere.spring.web.util.RequestValueSource.SESSION_ATTRIBUTE;
import static io.microsphere.spring.web.util.WebRequestUtils.PATH_ATTRIBUTE;
import static org.junit.Assert.assertEquals;

/**
 * {@link RequestValueSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestValueSource
 * @since 1.0.0
 */
public class RequestValueSourceTest {

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
        NativeWebRequest request = createWebRequest(r -> r.setParameter("test", "value"));
        assertEquals("value", PARAMETER.getValue(request, "test"));
    }

    @Test
    public void testGetValueForHEADER() {
        NativeWebRequest request = createWebRequest(r -> r.addHeader("test", "value"));
        assertEquals("value", HEADER.getValue(request, "test"));
    }

    @Test
    public void testGetValueForCOOKIE() {
        NativeWebRequest request = createWebRequest(r -> r.setCookies(new Cookie("test", "value")));
        assertEquals("value", COOKIE.getValue(request, "test"));
    }

    @Test
    public void testGetValueForBODY() {

    }

    @Test
    public void testGetValueForPATH_VARIABLE() {

    }

    @Test
    public void testGetValueForMATRIX_VARIABLE() {

    }
}