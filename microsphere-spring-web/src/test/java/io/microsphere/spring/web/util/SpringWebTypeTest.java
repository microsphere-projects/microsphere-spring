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


import io.microsphere.spring.web.context.request.MockServletWebRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.web.util.SpringWebType.UNKNOWN;
import static io.microsphere.spring.web.util.SpringWebType.WEBFLUX_INDICATOR_CLASS_NAME;
import static io.microsphere.spring.web.util.SpringWebType.WEBMVC_INDICATOR_CLASS_NAME;
import static io.microsphere.spring.web.util.SpringWebType.WEB_FLUX;
import static io.microsphere.spring.web.util.SpringWebType.WEB_MVC;
import static io.microsphere.spring.web.util.SpringWebType.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link SpringWebType} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringWebType
 * @since 1.0.0
 */
class SpringWebTypeTest {

    @Test
    void testConstants() {
        assertEquals("org.springframework.web.servlet.DispatcherServlet", WEBMVC_INDICATOR_CLASS_NAME);
        assertEquals("org.springframework.web.reactive.DispatcherHandler", WEBFLUX_INDICATOR_CLASS_NAME);
    }

    @Test
    void testValueOf() {
        assertSame(WEB_MVC, valueOf(new MockServletWebRequest()));

        NativeWebRequest request = mock(NativeWebRequest.class);
        assertSame(UNKNOWN, valueOf(request));

        when(request.getNativeRequest()).thenReturn(mock(ServerHttpRequest.class));
        assertSame(WEB_FLUX, valueOf(request));
    }

    @Test
    void testValueOfWithNull() {
        assertThrows(IllegalArgumentException.class, () -> valueOf((NativeWebRequest) null));
    }
}