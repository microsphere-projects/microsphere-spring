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

package io.microsphere.spring.webmvc.advice;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

import static io.microsphere.spring.web.servlet.util.WebUtils.getServletContext;
import static io.microsphere.spring.web.util.RequestAttributesUtils.HANDLER_METHOD_REQUEST_BODY_ARGUMENT_ATTRIBUTE_NAME_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * {@link StoringRequestBodyArgumentAdvice} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StoringRequestBodyArgumentAdvice
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        TestController.class,
        StoringRequestBodyArgumentAdvice.class,
        StoringRequestBodyArgumentAdviceTest.class
})
@EnableWebMvc
public class StoringRequestBodyArgumentAdviceTest extends WebMvcConfigurerAdapter {

    private static final String USER_ATTRIBUTE_NAME = "user";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private ServletContext servletContext;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptorAdapter() {
            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
                ServletContext servletContext = getServletContext(request);
                Enumeration<String> attributeNames = request.getAttributeNames();
                while (attributeNames.hasMoreElements()) {
                    String attributeName = attributeNames.nextElement();
                    if (attributeName.startsWith(HANDLER_METHOD_REQUEST_BODY_ARGUMENT_ATTRIBUTE_NAME_PREFIX)) {
                        servletContext.setAttribute(USER_ATTRIBUTE_NAME, request.getAttribute(attributeName));
                    }
                }
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        this.mockMvc = webAppContextSetup(this.context).build();
        this.servletContext = this.context.getServletContext();
    }

    @Test
    public void test() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);
        String json = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/test/user")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(json));

        User responsedUser = (User) this.servletContext.getAttribute(USER_ATTRIBUTE_NAME);
        assertEquals(user, responsedUser);
    }
}