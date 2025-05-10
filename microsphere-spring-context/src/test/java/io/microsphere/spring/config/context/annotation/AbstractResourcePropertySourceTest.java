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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.microsphere.io.FileUtils.forceDelete;
import static io.microsphere.spring.config.context.annotation.AbstractResourcePropertySourceTest.TARGET_RESOURCE_PATTERN;
import static io.microsphere.util.ClassLoaderUtils.getResource;
import static java.lang.Thread.sleep;
import static java.nio.file.Files.copy;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Abstract test class for {@link ResourcePropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ResourcePropertySource(
        autoRefreshed = true,
        value = TARGET_RESOURCE_PATTERN
)
@DirtiesContext
public class AbstractResourcePropertySourceTest {

    static final String SOURCE_DIR_PATH = "/META-INF/test/";

    static final String TARGET_DIR_NAME = "tmp";

    static final String TARGET_DIR_PATH = SOURCE_DIR_PATH + TARGET_DIR_NAME + "/";

    static final String SOURCE_RESOURCE_PATTERN = "classpath:" + SOURCE_DIR_PATH + "*.properties";

    static final String TARGET_RESOURCE_PATTERN_PREFIX = "classpath:" + TARGET_DIR_PATH;

    static final String TARGET_RESOURCE_PATTERN = TARGET_RESOURCE_PATTERN_PREFIX + "*.properties";

    @Autowired
    protected ConfigurableApplicationContext context;

    @Autowired
    protected Environment environment;

    @Value(TARGET_RESOURCE_PATTERN_PREFIX + "b.properties")
    protected Resource bPropertiesResource;

    protected File bPropertiesFile;

    @Before
    public void before() throws Throwable {
        assertNotNull(this.bPropertiesResource);
        assertTrue(this.bPropertiesResource.exists());
        this.bPropertiesFile = bPropertiesResource.getFile();
        assertTrue(this.bPropertiesFile.exists());
    }

    @BeforeClass
    public static void beforeClass() throws Throwable {
        URL sourceURL = getResource(SOURCE_DIR_PATH);
        File sourceDirectory = new File(sourceURL.getFile());
        File targetDirectory = new File(sourceDirectory, TARGET_DIR_NAME);
        delete(targetDirectory);
        targetDirectory.mkdirs();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources(SOURCE_RESOURCE_PATTERN);
        for (Resource resource : resources) {
            File resourceFile = resource.getFile();
            Path targetPath = targetDirectory.toPath().resolve(resource.getFilename());
            copy(resourceFile.toPath(), targetPath);
        }
    }

    static void delete(File file) throws IOException {
        if (file.exists()) {
            forceDelete(file);
        }
    }

    void assertOriginalProperties() {
        assertEquals("1", this.environment.getProperty("a"));
        assertEquals("3", this.environment.getProperty("b"));
    }

    void testOnFile(File targetFile, Properties properties) throws Throwable {

        // watches the properties file
        AtomicBoolean notified = new AtomicBoolean();

        String propertyName = targetFile.getName();
        String propertyValue = randomUUID().toString();

        this.context.addApplicationListener((ApplicationListener<PropertySourcesChangedEvent>) event -> {
            notified.set(true);
            assertOriginalProperties();
            assertEquals(propertyValue, this.environment.getProperty(propertyName));
        });

        // appends the new content
        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            properties.setProperty(propertyName, propertyValue);
            properties.store(outputStream, null);
        }

        // waits for being notified
        while (!notified.get()) {
            sleep(100);
        }
    }
}
