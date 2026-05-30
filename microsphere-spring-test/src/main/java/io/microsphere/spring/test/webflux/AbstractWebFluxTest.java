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
package io.microsphere.spring.test.webflux;

import io.microsphere.logging.Logger;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Locale.ENGLISH;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToApplicationContext;
import static reactor.core.publisher.Mono.just;

/**
 * Abstract WebFlux Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Disabled
@SpringJUnitConfig(classes = {
        TestController.class,           // Test Controller
        RouterFunctionTestConfig.class  // Test RouterFunction
})
@EnableWebFlux
public abstract class AbstractWebFluxTest {

    protected final Logger logger = getLogger(this.getClass());

    @Autowired
    protected ConfigurableApplicationContext context;

    @Autowired
    protected TestController testController;

    protected WebTestClient webTestClient;

    @BeforeEach
    final void init() {
        this.webTestClient = buildWebTestClient(this.context);
    }

    /**
     * Test the Web Endpoints
     *
     * @see #testHelloWorld()
     * @see #testGreeting()
     * @see #testUser()
     * @see #testError()
     * @see #testResponseEntity()
     * @see #testUpdatePerson()
     */
    protected void testWebEndpoints() {
        this.testHelloWorld();
        this.testGreeting();
        this.testUser();
        this.testError();
        this.testResponseEntity();
        this.testUpdatePerson();
    }

    /**
     * Test {@link TestController#helloWorld()}
     */
    protected void testHelloWorld() {
        this.webTestClient.get()
                .uri("/test/helloworld")
                .header(ACCEPT_LANGUAGE, ENGLISH.getLanguage())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(this.testController.helloWorld());
    }

    /**
     * Test {@link TestController#greeting(String)}
     */
    protected void testGreeting() {
        String pattern = "/test/greeting/{message}";
        String message = "Mercy";
        this.webTestClient.get()
                .uri(pattern, message)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(this.testController.greeting(message));
    }

    /**
     * Test {@link TestController#user(User)}
     */
    protected void testUser() {
        String pattern = "/test/user";
        User user = new User();
        user.setName("Mercy");
        user.setAge(18);

        Mono<User> userMono = just(user);
        this.webTestClient.post()
                .uri(pattern)
                .accept(APPLICATION_JSON)
                .body(userMono, User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(this.testController.user(user));
    }

    /**
     * Test {@link TestController#error(String)}
     */
    protected void testError() {
        this.webTestClient.get()
                .uri("/test/error?message=hello")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    /**
     * Test {@link TestController#responseEntity()}
     */
    protected void testResponseEntity() {
        this.webTestClient.put()
                .uri("/test/response-entity")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(this.testController.responseEntity().getBody());
    }

    /**
     * @see RouterFunctionTestConfig#nestedPersonRouterFunction(PersonHandler)
     */
    protected void testUpdatePerson() {
        this.webTestClient.put()
                .uri("/test/person/{id}", "1")
                .exchange()
                .expectStatus().isOk();
    }

    protected WebTestClient buildWebTestClient(ConfigurableApplicationContext context) {
        return bindToApplicationContext(context)
                .build();
    }
}