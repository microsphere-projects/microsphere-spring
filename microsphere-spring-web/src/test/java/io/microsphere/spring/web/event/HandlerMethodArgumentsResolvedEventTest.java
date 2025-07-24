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


import io.microsphere.spring.web.AbstractSpringWebTest;
import io.microsphere.spring.web.controller.TestController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * {@link HandlerMethodArgumentsResolvedEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodArgumentsResolvedEvent
 * @since 1.0.0
 */
public class HandlerMethodArgumentsResolvedEventTest extends AbstractSpringWebTest {

    private HandlerMethod handlerMethod;

    private HandlerMethodArgumentsResolvedEvent event;

    @Before
    public void setUp() throws Throwable {
        this.handlerMethod = new HandlerMethod(new TestController(), "helloWorld");
        event = new HandlerMethodArgumentsResolvedEvent(createWebRequest("/helloworld"), this.handlerMethod, ofArray("Hello", "World"));
    }

    @Test
    public void testGetWebRequest() {
        NativeWebRequest nativeWebRequest = (NativeWebRequest) this.event.getSource();
        assertSame(nativeWebRequest, this.event.getWebRequest());

        MockHttpServletRequest request = (MockHttpServletRequest) nativeWebRequest.getNativeRequest();
        assertEquals("/helloworld", request.getRequestURI());
    }

    @Test
    public void testGetHandlerMethod() {
        assertSame(this.handlerMethod, this.event.getHandlerMethod());
    }

    @Test
    public void testGetMethod() throws NoSuchMethodException {
        assertEquals(TestController.class.getMethod("helloWorld"), this.event.getMethod());
    }

    @Test
    public void testGetArguments() {
        assertArrayEquals(ofArray("Hello", "World"), this.event.getArguments());
    }

    @Test
    public void testToString() {
        assertNotNull(this.event.toString());
    }

}