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
import org.junit.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.web.util.WebType.NONE;
import static io.microsphere.spring.web.util.WebType.REACTIVE;
import static io.microsphere.spring.web.util.WebType.SERVLET;
import static io.microsphere.spring.web.util.WebType.valueOf;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link WebType} test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebType
 * @since 1.0.0
 */
public class WebTypeTest {

    @Test
    public void testValueOf() {
        assertSame(SERVLET, valueOf(new MockServletWebRequest()));

        NativeWebRequest request = mock(NativeWebRequest.class);
        assertSame(NONE, valueOf(request));

        when(request.getNativeRequest()).thenReturn(mock(ServerHttpRequest.class));
        assertSame(REACTIVE, valueOf(request));
    }

    @Test
    public void testValueOfWithNull() {
        assertThrows(IllegalArgumentException.class, () -> valueOf((NativeWebRequest) null));
    }
}