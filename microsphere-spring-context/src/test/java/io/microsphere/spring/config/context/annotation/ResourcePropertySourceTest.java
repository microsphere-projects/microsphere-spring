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
package io.microsphere.spring.config.context.annotation;

import io.microsphere.spring.config.env.event.PropertySourcesChangedEvent;
import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.microsphere.spring.config.context.annotation.ResourcePropertySourceTestOnFileCreated.C_PROPERTIES_FILE_NAME;
import static java.lang.Thread.sleep;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadProperties;

/**
 * {@link ResourcePropertySource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        ResourcePropertySourceTest.class
})
public class ResourcePropertySourceTest extends AbstractResourcePropertySourceTest {

    @Test
    public void testOnFileCreated() throws Throwable {
        File cPropertiesFile = new File(this.bPropertiesFile.getParentFile(), C_PROPERTIES_FILE_NAME);
        Properties cProperties = new Properties();
        testOnFile(cPropertiesFile, cProperties);
    }

    @Test
    public void testOnFileModified() throws Throwable {
        Properties bProperties = loadProperties(this.bPropertiesResource);
        testOnFile(this.bPropertiesFile, bProperties);
    }

    @Test
    public void testOnFileDeleted() throws Throwable {
        // watches the properties file
        AtomicBoolean notified = new AtomicBoolean();

        this.context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) event -> {
            notified.set(true);
            Map<String, Object> removedProperties = event.getRemovedProperties();
            assertEquals(singletonMap("b", "2"), removedProperties);
        });

        delete(this.bPropertiesFile);

        // waits for being notified
        while (!notified.get()) {
            sleep(100);
        }
    }
}

