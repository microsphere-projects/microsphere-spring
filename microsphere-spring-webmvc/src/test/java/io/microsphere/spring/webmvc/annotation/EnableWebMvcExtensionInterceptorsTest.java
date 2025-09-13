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
package io.microsphere.spring.webmvc.annotation;

import io.microsphere.spring.webmvc.interceptor.IdempotentAnnotatedMethodHandlerInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import static io.microsphere.spring.webmvc.interceptor.IdempotentAnnotatedMethodHandlerInterceptor.MOCK_TOKEN_VALUE;
import static io.microsphere.spring.webmvc.interceptor.IdempotentAnnotatedMethodHandlerInterceptor.TOKEN_HEADER_NAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link EnableWebMvcExtension} Test with interceptors
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        EnableWebMvcExtensionInterceptorsTest.class
})
@EnableWebMvcExtension(handlerInterceptors = {
        IdempotentAnnotatedMethodHandlerInterceptor.class
})
class EnableWebMvcExtensionInterceptorsTest extends AbstractEnableWebMvcExtensionTest {

    @Test
    public void testWebEndpoints() throws Exception {
        this.mockMvc.perform(get("/test/greeting/hello").header(TOKEN_HEADER_NAME, MOCK_TOKEN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string("Greeting : hello"));
    }
}
