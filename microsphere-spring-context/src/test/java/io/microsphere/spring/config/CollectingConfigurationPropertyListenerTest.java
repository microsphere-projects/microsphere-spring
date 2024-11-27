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
package io.microsphere.spring.config;

import io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer;
import io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CollectingConfigurationPropertyListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since CollectingConfigurationPropertyListener
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = CollectingConfigurationPropertyListenerTest.class,
        initializers = {
                ListenableConfigurableEnvironmentInitializer.class,
                ListenableAutowireCandidateResolverInitializer.class,
                CollectingConfigurationPropertyInitializer.class
        }
)
@TestPropertySource(
        properties = {
                "microsphere.spring.listenable-environment.enabled=true",
                "microsphere.spring.listenable-autowire-candidate-resolver.enabled=true",
                "spring.profiles.active=test",
                "user.name=Mercy",
                "score=99"
        }
)
class CollectingConfigurationPropertyListenerTest {

    @Value("${user.name}")
    private String userName;

    @Value("${score}")
    private Integer score;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private ConfigurationPropertyRepository configurationPropertyRepository;

    @Test
    public void test() {
        String profiles = environment.resolvePlaceholders("${spring.profiles.active}");
        assertEquals("test", profiles);

        ConfigurationProperty configurationProperty = configurationPropertyRepository.get("user.name");
        assertEquals(String.class, configurationProperty.getType());
        assertEquals("Mercy", configurationProperty.getValue());
        assertTrue(configurationProperty.isRequired());
        assertFalse(configurationProperty.getMetadata().getTargets().isEmpty());
    }
}
