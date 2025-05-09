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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadProperties;

/**
 * {@link ResourcePropertySource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ResourcePropertySourceTest.class
})
@ResourcePropertySource(
        autoRefreshed = true,
        value = {
                "classpath*:/META-INF/test/*.properties"
        }
)
public class ResourcePropertySourceTest {

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableApplicationContext context;

    @Value("classpath:/META-INF/test/b.properties")
    private Resource bPropertiesResource;


    @Test
    public void test() {

    }

    @Test
    public void testOnFileModified() throws Exception {
        File bPropertiesFile = bPropertiesResource.getFile();
        assertTrue(bPropertiesFile.exists());

        Properties bProperties = loadProperties(bPropertiesResource);

        assertEquals("2", bProperties.getProperty("b"));

        // watches the properties file
        AtomicBoolean modified = new AtomicBoolean();

        String propertyName = "d";
        String propertyValue = new Date().toString();

        context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) event -> {
            modified.set(true);
        });

        // appends the new content
        try (OutputStream outputStream = new FileOutputStream(bPropertiesFile)) {
            bProperties.setProperty(propertyName, propertyValue);
            bProperties.store(outputStream, null);
        }

        // waits for being notified
        while (!modified.get()) {
            sleep(100);
        }

        synchronized (environment) {
            assertEquals("1", environment.getProperty("a"));
            assertEquals("3", environment.getProperty("b"));
            assertEquals(propertyValue, environment.getProperty(propertyName));
        }
    }
}

