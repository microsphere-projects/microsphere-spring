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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurationPropertyRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationPropertyRepository
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                ConfigurationPropertyRepository.class,
                ConfigurationPropertyRepositoryTest.class,
        }
)
@TestPropertySource(
        properties = {
                "microsphere.spring.config-property-repository.max-size=3"
        }
)
public class ConfigurationPropertyRepositoryTest {

    @Autowired
    private ConfigurationPropertyRepository repository;

    @Test
    public void test() throws Exception {
        String name = "test";
        ConfigurationProperty configurationProperty = new ConfigurationProperty(name);

        assertEquals(3, repository.getMaxSize());

        repository.add(configurationProperty);

        assertTrue(repository.contains(name));
        assertEquals(configurationProperty, repository.get(name));
        assertEquals(configurationProperty, repository.remove(name));

        Collection<ConfigurationProperty> configurationProperties = repository.getAll();
        assertTrue(configurationProperties.isEmpty());

        repository.createIfAbsent(name);
        configurationProperties = repository.getAll();
        assertFalse(configurationProperties.isEmpty());

        assertThrows(IllegalStateException.class, () -> {
            repository.createIfAbsent("1");
            repository.createIfAbsent("2");
            repository.createIfAbsent("3");
        });

        repository.destroy();
        configurationProperties = repository.getAll();
        assertTrue(configurationProperties.isEmpty());
    }
}
