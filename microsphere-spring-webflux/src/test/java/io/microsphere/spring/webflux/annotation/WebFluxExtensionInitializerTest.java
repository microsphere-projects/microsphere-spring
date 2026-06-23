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

package io.microsphere.spring.webflux.annotation;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.util.List;

import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactoryNames;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link io.microsphere.spring.webflux.annotation.WebFluxExtensionInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see io.microsphere.spring.webflux.annotation.WebFluxExtensionInitializer
 * @since 1.0.0
 */
@SpringJUnitConfig(
        classes = {
                WebFluxExtensionInitializerTest.class
        },
        initializers = {
                WebFluxExtensionInitializer.class
        }
)
@EnableWebFlux
class WebFluxExtensionInitializerTest {

    @Autowired
    private WebFluxExtensionInitializer webFluxExtensionInitializer;

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    void test() {
        webFluxExtensionInitializer.initialize(context);
        assertEquals("microsphere.spring.webflux.enabled", webFluxExtensionInitializer.getEnabledPropertyName());
        assertTrue(webFluxExtensionInitializer.getDefaultEnabled());

        List<String> factoryClassNames = loadFactoryNames(ApplicationContextInitializer.class);
        assertTrue(factoryClassNames.contains(WebFluxExtensionInitializer.class.getName()));
    }
}