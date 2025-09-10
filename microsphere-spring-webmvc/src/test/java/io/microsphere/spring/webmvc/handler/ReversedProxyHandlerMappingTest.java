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

package io.microsphere.spring.webmvc.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.ID_HEADER_NAME;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * {@link ReversedProxyHandlerMapping} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ReversedProxyHandlerMapping
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = {
        TestController.class,
        ReversedProxyHandlerMapping.class,
        ReversedProxyHandlerMappingTest.class
})
@EnableWebMvc
@EnableWebMvcExtension
class ReversedProxyHandlerMappingTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private WebEndpointMappingRegistry webEndpointMappingRegistry;

    private Map<String, WebEndpointMapping> webEndpointMappingsMap;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context).build();
        initWebEndpointMappingsMap();
    }

    private void initWebEndpointMappingsMap() {
        Collection<WebEndpointMapping> webEndpointMappings = this.webEndpointMappingRegistry.getWebEndpointMappings();
        this.webEndpointMappingsMap = new HashMap<>(webEndpointMappings.size());
        for (WebEndpointMapping webEndpointMapping : webEndpointMappings) {
            this.webEndpointMappingsMap.put(webEndpointMapping.getPatterns()[0], webEndpointMapping);
        }
    }

    @Test
    void testHelloWorld() throws Exception {
        String pattern = "/test/helloworld";
        this.mockMvc.perform(get(pattern).header(ID_HEADER_NAME, getWebEndpointMappingId(pattern)))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello World"));
    }

    @Test
    void testGreeting() throws Exception {
        String pattern = "/test/greeting/{message}";
        this.mockMvc.perform(get(pattern, "Mercy"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greeting : Mercy"));
    }

    @Test
    void testUser() throws Exception {
        String pattern = "/test/user";
        ObjectMapper objectMapper = new ObjectMapper();
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);
        String json = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post(pattern)
                        .header(ID_HEADER_NAME, getWebEndpointMappingId(pattern))
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(json));
    }

    @Test
    void testError() {
        String pattern = "/test/error";
        assertThrows(ServletException.class, () ->
                this.mockMvc.perform(get(pattern)
                        .header(ID_HEADER_NAME, getWebEndpointMappingId(pattern))
                        .param("message", "For testing")
                        .header(ID_HEADER_NAME, currentTimeMillis())
                ).andReturn());
    }

    @Test
    void testResponseEntity() throws Exception {
        String pattern = "/test/response-entity";
        this.mockMvc.perform(put(pattern).header(ID_HEADER_NAME, currentTimeMillis()))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void testView() throws Exception {
        String pattern = "/test/view";
        this.mockMvc.perform(get("/test/view").header(ID_HEADER_NAME, getWebEndpointMappingId(pattern)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

//    @Test
//    void testInvokeGetHandlerExecutionChainOnFailed() {
//        ReversedProxyHandlerMapping mapping = this.context.getBean(ReversedProxyHandlerMapping.class);
//        String pattern = "/test/greeting/{message}";
//        WebEndpointMapping webEndpointMapping = this.webEndpointMappingsMap.get(pattern);
//        assertNull(mapping.invokeGetHandlerExecutionChain(mapping, webEndpointMapping.getEndpoint(), new MockHttpServletRequest()));
//    }

    private int getWebEndpointMappingId(String pattern) {
        WebEndpointMapping webEndpointMapping = this.webEndpointMappingsMap.get(pattern);
        return webEndpointMapping.getId();
    }
}