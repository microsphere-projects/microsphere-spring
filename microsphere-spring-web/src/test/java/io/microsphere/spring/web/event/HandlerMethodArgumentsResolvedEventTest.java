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

package io.microsphere.spring.web.event;


import io.microsphere.spring.test.web.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link HandlerMethodArgumentsResolvedEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodArgumentsResolvedEvent
 * @since 1.0.0
 */
public class HandlerMethodArgumentsResolvedEventTest {

    private HandlerMethod handlerMethod;

    private HandlerMethodArgumentsResolvedEvent event;

    @BeforeEach
    void setUp() throws Throwable {
        this.handlerMethod = new HandlerMethod(new TestController(), "helloWorld");
        event = new HandlerMethodArgumentsResolvedEvent(createWebRequest("/helloworld"), this.handlerMethod, ofArray("Hello", "World"));
    }

    @Test
    void testGetWebRequest() {
        NativeWebRequest nativeWebRequest = (NativeWebRequest) this.event.getSource();
        assertSame(nativeWebRequest, this.event.getWebRequest());

        MockHttpServletRequest request = (MockHttpServletRequest) nativeWebRequest.getNativeRequest();
        assertEquals("/helloworld", request.getRequestURI());
    }

    @Test
    void testGetHandlerMethod() {
        assertSame(this.handlerMethod, this.event.getHandlerMethod());
    }

    @Test
    void testGetMethod() throws NoSuchMethodException {
        assertEquals(TestController.class.getMethod("helloWorld"), this.event.getMethod());
    }

    @Test
    void testGetArguments() {
        assertArrayEquals(ofArray("Hello", "World"), this.event.getArguments());
    }

    @Test
    void testToString() {
        assertNotNull(this.event.toString());
    }

}