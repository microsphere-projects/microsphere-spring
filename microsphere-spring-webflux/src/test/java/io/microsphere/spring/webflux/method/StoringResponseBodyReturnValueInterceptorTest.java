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

package io.microsphere.spring.webflux.method;


import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.webflux.context.request.ServerWebRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.spring.web.util.RequestAttributesUtils.getHandlerMethodReturnValue;
import static io.microsphere.spring.webflux.test.WebTestUtils.mockServerWebExchange;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link StoringResponseBodyReturnValueInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StoringResponseBodyReturnValueInterceptor
 * @since 1.0.0
 */
class StoringResponseBodyReturnValueInterceptorTest {

    private static final String returnValue = "Hello World";

    private StoringResponseBodyReturnValueInterceptor interceptor;

    private TestController testController;

    @BeforeEach
    void setUp() {
        this.interceptor = new StoringResponseBodyReturnValueInterceptor();
        this.testController = new TestController();
    }

    @Test
    void testAfterExecuteWithNullReturnValue() throws Exception {
        interceptor.afterExecute(null, null, null, null, null);
    }

    @Test
    void testAfterExecuteWithoutResponseBody() throws Exception {
        HandlerMethod handlerMethod = new HandlerMethod(testController, "view");
        interceptor.afterExecute(handlerMethod, null, returnValue, null, null);
    }

    @Test
    void testAfterExecute() throws Exception {
        HandlerMethod handlerMethod = new HandlerMethod(testController, "helloWorld");
        MockServerWebExchange serverWebExchange = mockServerWebExchange();
        NativeWebRequest webRequest = new ServerWebRequest(serverWebExchange);
        interceptor.afterExecute(handlerMethod, null, returnValue, null, webRequest);
        assertSame(returnValue, getHandlerMethodReturnValue(webRequest, handlerMethod));
    }
}