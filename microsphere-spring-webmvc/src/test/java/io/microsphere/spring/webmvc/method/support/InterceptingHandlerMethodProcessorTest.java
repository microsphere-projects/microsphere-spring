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

package io.microsphere.spring.webmvc.method.support;


import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import io.microsphere.spring.webmvc.test.AbstractWebMvcTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.ServletException;
import java.lang.reflect.Method;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link InterceptingHandlerMethodProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InterceptingHandlerMethodProcessor
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        LoggingHandlerMethodArgumentResolverAdvice.class,
        InterceptingHandlerMethodProcessorTest.class,
})
@EnableWebMvcExtension(registerHandlerInterceptors = true)
@Import(TestController.class)
public class InterceptingHandlerMethodProcessorTest extends AbstractWebMvcTest {

    @Autowired
    private InterceptingHandlerMethodProcessor processor;

    @Autowired
    private TestController testController;

    private Method greetingMethod;

    private HandlerMethod greetingHandlerMethod;

    private MethodParameter greetingMethodParameter0;

    @Before
    public void setUp() {
        super.setUp();
        this.greetingMethod = findMethod(TestController.class, "greeting", String.class);
        this.greetingHandlerMethod = new HandlerMethod(testController, greetingMethod);
        this.greetingMethodParameter0 = new MethodParameter(greetingMethod, 0);
    }

    @Test
    public void test() throws Exception {
        this.mockMvc.perform(get("/test/greeting/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greeting : hello"));
    }

    @Test
    public void testError() {
        assertThrows(ServletException.class, () ->
                this.mockMvc.perform(get("/test/error").param("message", "For testing"))
                        .andReturn());
    }

    @Test
    public void testSupportsParameter() {
        assertTrue(this.processor.supportsParameter(greetingMethodParameter0));
    }

    @Test
    public void testSupportsParameterWithUnsupportedMethodParameter() {
        Method helloWorldMethod = findMethod(TestController.class, "helloWorld");
        MethodParameter methodParameter = new MethodParameter(helloWorldMethod, -1);
        assertFalse(this.processor.supportsParameter(methodParameter));
    }

    @Test
    public void testSupportsReturnTypeWithResponseBodyResult() {
        assertTrue(this.processor.supportsReturnType(greetingHandlerMethod.getReturnType()));
    }

    @Test
    public void testSupportsReturnTypeWithResponseEntityResult() {
        Method responseEntityMethod = findMethod(TestController.class, "responseEntity");
        MethodParameter methodParameter = new MethodParameter(responseEntityMethod, -1);
        assertTrue(this.processor.supportsReturnType(methodParameter));
    }

    @Test
    public void testSupportsWithModelAndViewResult() {
        Method viewMethod = findMethod(TestController.class, "view");
        MethodParameter methodParameter = new MethodParameter(viewMethod, -1);
        assertTrue(this.processor.supportsReturnType(methodParameter));
    }

    @Test
    public void testSupportsWithUnsupportedHandlerResult() {
        Method hashCodeMethod = findMethod(Object.class, "hashCode");
        MethodParameter methodParameter = new MethodParameter(hashCodeMethod, -1);
        assertFalse(this.processor.supportsReturnType(methodParameter));
    }

    @Test
    public void testResolveArgumentResolverWithEmptyArgumentResolver() {
        assertNull(this.processor.resolveArgumentResolver(greetingMethodParameter0, emptyList()));
    }

    @Test
    public void testResolveReturnValueHandlerWithEmptyReturnValueHandler() {
        Method helloWorldMethod = findMethod(TestController.class, "helloWorld");
        HandlerMethod handlerMethod = new HandlerMethod(testController, helloWorldMethod);
        assertNull(this.processor.resolveReturnValueHandler(handlerMethod, emptyList()));
    }

    @Test
    public void testResolveArguments() {
        NativeWebRequest webRequest = createWebRequest();
        assertNull(this.processor.resolveArguments(webRequest, greetingMethodParameter0, null));
    }

    @Test
    public void testPreHandle() {
        assertTrue(this.processor.preHandle(null, null, null));
    }

    @Test
    public void testPostHandle() {
        this.processor.postHandle(null, null, null, null);
    }

    @Test
    public void testAfterCompletion() throws Exception {
        this.processor.afterCompletion(null, null, null, null);
    }

    @Test
    public void testAfterExecuteOnNotHandlerMethod() throws Exception {
        this.processor.afterExecute(null, null, null);
    }
}