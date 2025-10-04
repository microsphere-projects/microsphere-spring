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

package io.microsphere.spring.webmvc.idempotent;


import io.microsphere.spring.web.idempotent.DefaultIdempotentService;
import io.microsphere.spring.web.idempotent.Idempotent;
import io.microsphere.spring.web.idempotent.IdempotentAttributes;
import io.microsphere.spring.web.idempotent.IdempotentService;
import io.microsphere.spring.webmvc.annotation.AbstractEnableWebMvcExtensionTest;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import io.microsphere.spring.webmvc.test.EnableWebMvcExtensionInterceptorsTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.lang.reflect.Method;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.idempotent.IdempotentAttributes.of;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link IdempotentAnnotatedMethodHandlerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see IdempotentAnnotatedMethodHandlerInterceptor
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        DefaultIdempotentService.class,
        IdempotentAnnotatedMethodHandlerInterceptorTest.class,
        EnableWebMvcExtensionInterceptorsTestConfig.class
})
@EnableWebMvcExtension(handlerInterceptors = {
        IdempotentAnnotatedMethodHandlerInterceptor.class
})
@RestController
class IdempotentAnnotatedMethodHandlerInterceptorTest extends AbstractEnableWebMvcExtensionTest {

    @Autowired
    private IdempotentService idempotentService;

    @PostMapping("/idempotent")
    @Idempotent
    public String idempotent() {
        return "idempotent";
    }

    @Test
    protected void testWebEndpoints() throws Exception {
        super.testWebEndpoints();
        NativeWebRequest request = createWebRequest();
        Method method = findMethod(IdempotentAnnotatedMethodHandlerInterceptorTest.class, "idempotent");
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        IdempotentAttributes attributes = of(idempotent);

        String token = idempotentService.renewToken(request, attributes);
        this.mockMvc.perform(
                        post("/idempotent")
                                .header(attributes.getTokenName(), token)
                ).andExpect(status().isOk())
                .andExpect(content().string(this.idempotent()));
    }
}