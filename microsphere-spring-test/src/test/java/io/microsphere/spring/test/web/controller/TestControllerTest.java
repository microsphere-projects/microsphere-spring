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

package io.microsphere.spring.test.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.spring.test.domain.User;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * {@link TestController} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestController
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        TestController.class,
        TestControllerTest.class
})
@EnableWebMvc
class TestControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @Test
    void testHelloWorld() throws Exception {
        this.mockMvc.perform(get("/test/helloworld"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    void testGreeting() throws Exception {
        this.mockMvc.perform(get("/test/greeting/{message}", "Mercy"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greeting : Mercy"));
    }

    @Test
    void testUser() throws Exception {
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
    }

    @Test
    void testError() {
        assertThrows(ServletException.class, () ->
                this.mockMvc.perform(get("/test/error")
                                .param("message", "For testing"))
                        .andReturn());
    }

    @Test
    void testResponseEntity() throws Exception {
        this.mockMvc.perform(put("/test/response-entity"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void testView() throws Exception {
        this.mockMvc.perform(get("/test/view"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}