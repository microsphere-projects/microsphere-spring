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
package io.microsphere.spring.config.env.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * {@link YamlPropertySource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see YamlPropertySource
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        YamlPropertySourceTest.class
})
@YamlPropertySource(value = {
        "classpath*:/META-INF/test/yaml/*.yaml"
}, autoRefreshed = true
)
public class YamlPropertySourceTest {

    @Value("${my.name}")
    private String myName;

    @Value("classpath*:/META-INF/test/yaml/*.yaml")
    private Resource[] resources;

    @Autowired
    private Environment environment;

    @Test
    public void test() throws Exception {
        assertEquals("mercyblitz", myName);

        String name = "Mercy Ma @ " + new Date();
        write(1, "my.name2 : " + name);

        while (!Objects.equals(name, environment.getProperty("my.name2"))) {
            Thread.sleep(1000 * 1L);
        }
    }

    private void write(int resourceIndex, String content) throws Exception {
        Resource resource = resources[resourceIndex];
        File file = resource.getFile();
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }
}
