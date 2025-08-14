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
package io.microsphere.spring.webflux;

import io.microsphere.spring.webflux.annotation.EnableWebFluxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;

import static org.springframework.test.web.reactive.server.WebTestClient.bindToApplicationContext;

/**
 * Abstract WebFlux Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebFluxExtension
 * @since 1.0.0
 */
@SpringJUnitConfig
@EnableWebFlux
@Disabled
public abstract class AbstractWebFluxTest {

    @Autowired
    protected ConfigurableApplicationContext context;

    protected WebTestClient webTestClient;

    @BeforeEach
    final void init() {
        this.webTestClient = buildWebTestClient(this.context);
    }

    public static WebTestClient buildWebTestClient(ConfigurableApplicationContext context) {
        return bindToApplicationContext(context).build();
    }
}
