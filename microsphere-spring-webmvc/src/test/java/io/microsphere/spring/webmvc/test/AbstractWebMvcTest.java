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

package io.microsphere.spring.webmvc.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletException;

import static org.junit.Assert.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Abstract class for WebMVC testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvc
 * @since 1.0.0
 */
@Ignore
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        TestController.class           // Test Controller
})
@EnableWebMvc
public abstract class AbstractWebMvcTest {

    @Autowired
    protected ConfigurableWebApplicationContext context;

    @Autowired
    protected ObjectProvider<TestController> testControllerProvider;

    protected MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.context).build();
    }

    /**
     * Test {@link TestController#helloWorld()}
     *
     * @throws Exception If failed to execute {@link MockMvc#perform(RequestBuilder)}
     */
    public void testHelloWorld() throws Exception {
        this.mockMvc.perform(get("/test/helloworld"))
                .andExpect(status().isOk())
                .andExpect(content().string(this.getTestController().helloWorld()));
    }

    /**
     * Test {@link TestController#helloWorld()}
     *
     * @throws Exception If failed to execute {@link MockMvc#perform(RequestBuilder)}
     */
    public void testGreeting() throws Exception {
        String pattern = "/test/greeting/{message}";
        String message = "Mercy";
        this.mockMvc.perform(get(pattern, message))
                .andExpect(status().isOk())
                .andExpect(content().string(this.getTestController().greeting(message)));
    }

    /**
     * Test {@link TestController#helloWorld()}
     *
     * @throws Exception If failed to execute {@link MockMvc#perform(RequestBuilder)}
     */
    public void testUser() throws Exception {
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

    /**
     * Test {@link TestController#helloWorld()}
     *
     * @throws Exception If failed to execute {@link MockMvc#perform(RequestBuilder)}
     */
    public void testError() {
        assertThrows(ServletException.class, () -> this.mockMvc.perform(get("/test/error")
                .param("message", "For testing")).andReturn());
    }

    /**
     * Test {@link TestController#helloWorld()}
     *
     * @throws Exception If failed to execute {@link MockMvc#perform(RequestBuilder)}
     */
    public void testResponseEntity() throws Exception {
        this.mockMvc.perform(put("/test/response-entity"))
                .andExpect(status().isOk())
                .andExpect(content().string(this.getTestController().responseEntity().getBody()));
    }

    /**
     * Test {@link TestController#helloWorld()}
     *
     * @throws Exception If failed to execute {@link MockMvc#perform(RequestBuilder)}
     */
    public void testView() throws Exception {
        this.mockMvc.perform(get("/test/view"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    protected TestController getTestController() {
        return this.testControllerProvider.getIfAvailable();
    }
}
