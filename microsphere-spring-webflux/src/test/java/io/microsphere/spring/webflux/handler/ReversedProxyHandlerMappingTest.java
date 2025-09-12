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

package io.microsphere.spring.webflux.handler;


import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import io.microsphere.spring.webflux.annotation.EnableWebFluxExtension;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.ID_HEADER_NAME;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.servlet;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.webflux;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

/**
 * {@link ReversedProxyHandlerMapping} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ReversedProxyHandlerMapping
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        ReversedProxyHandlerMappingTest.class,
})
@EnableWebFluxExtension(reversedProxyHandlerMapping = true)
class ReversedProxyHandlerMappingTest extends AbstractWebFluxTest {

    @Autowired
    private ReversedProxyHandlerMapping mapping;

    @Autowired
    private WebEndpointMappingRegistry webEndpointMappingRegistry;

    private Map<String, WebEndpointMapping> webEndpointMappingsMap;

    @BeforeEach
    void setUp() {
        initWebEndpointMappingsMap();
    }

    private void initWebEndpointMappingsMap() {
        Collection<WebEndpointMapping> webEndpointMappings = this.webEndpointMappingRegistry.getWebEndpointMappings();
        this.webEndpointMappingsMap = new HashMap<>(webEndpointMappings.size());
        for (WebEndpointMapping webEndpointMapping : webEndpointMappings) {
            this.webEndpointMappingsMap.put(webEndpointMapping.getPatterns()[0], webEndpointMapping);
        }
    }

    /**
     * Test {@link TestController#greeting(String)} without the header {@link WebEndpointMapping#ID_HEADER_NAME}
     */
    @Test
    protected void testGreeting() {
        super.testGreeting();
    }

    /**
     * Test {@link TestController#user(User)} with the header {@link WebEndpointMapping#ID_HEADER_NAME}
     */
    @Test
    protected void testUser() {
        String pattern = "/test/user";
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);

        Mono<User> userMono = just(user);
        this.webTestClient.post()
                .uri(pattern)
                .header(ID_HEADER_NAME, getWebEndpointMappingId(pattern))
                .accept(APPLICATION_JSON)
                .body(userMono, User.class)
                .exchange()
                .expectBody(User.class);
    }

    /**
     * Test {@link TestController#responseEntity()} with the header {@link WebEndpointMapping#ID_HEADER_NAME}
     * that was not found
     */
    @Test
    protected void testResponseEntity() {
        this.webTestClient.put()
                .uri("/test/response-entity")
                .header(ID_HEADER_NAME, valueOf(currentTimeMillis()))
                .exchange()
                .expectBody(String.class)
                .isEqualTo("OK");
    }

    @Test
    void testGetHandlerInternalOnServletWebEndpointMapping() {
        WebEndpointMapping<?> webEndpointMapping = servlet()
                .method(GET)
                .pattern("/test/servlet")
                .endpoint(this)
                .source(this)
                .build();
        assertEquals(empty(), this.mapping.getHandlerInternal(webEndpointMapping));
    }

    @Test
    void testGetHandlerInternalOnWebFluxWebEndpointMapping() {
        WebEndpointMapping<?> webEndpointMapping = webflux()
                .method(GET)
                .pattern("/test/webflux")
                .endpoint(this)
                .source(this)
                .build();
        assertEquals(empty(), this.mapping.getHandlerInternal(webEndpointMapping));
    }

    private String getWebEndpointMappingId(String pattern) {
        WebEndpointMapping webEndpointMapping = this.webEndpointMappingsMap.get(pattern);
        return valueOf(webEndpointMapping.getId());
    }
}