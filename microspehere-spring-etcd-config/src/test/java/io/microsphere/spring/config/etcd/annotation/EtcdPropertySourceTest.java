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
package io.microsphere.spring.config.etcd.annotation;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.microsphere.spring.config.env.support.JsonPropertySourceFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * {@link EtcdPropertySource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EtcdPropertySourceTest.class,
        EtcdPropertySourceTest.Config.class
})
public class EtcdPropertySourceTest {

    @Autowired
    private Environment environment;

    private static Client client;

    @BeforeClass
    public static void init() throws Exception {
        EtcdPropertySource annotation =
                EtcdPropertySourceTest.Config.class.getAnnotation(EtcdPropertySource.class);

        client = buildClient(annotation);

        // 添加模拟数据
        mockConfig();
    }

    private static Client buildClient(EtcdPropertySource annotation) throws Exception {
        Client client = Client.builder()
                .endpoints(annotation.endpoints())
                .build();
        return client;
    }

    private static void mockConfig() throws Exception {

        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = patternResolver.getResources("classpath:/META-INF/etcd/*.json");

        for (Resource resource : resources) {
            // test.json
            String fileName = resource.getFilename();
            String key = fileName;
            byte[] data = StreamUtils.copyToByteArray(resource.getInputStream());
            writeConfig(key, data);
        }
    }

    private static void writeConfig(String stringKey, byte[] data) throws Exception {
        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from(stringKey.getBytes());
        ByteSequence value = ByteSequence.from(data);
        // put the key-value
        kvClient.put(key, value).get();
    }

    @AfterClass
    public static void destroy() {
        client.close();
    }


    @Test
    public void test() throws Exception {
        assertEquals("mercyblitz", environment.getProperty("my.name"));

        writeConfig("test.json", "{ \"my.name\": \"Mercy Ma\" }".getBytes(StandardCharsets.UTF_8));

        Thread.sleep(5 * 1000);

        assertEquals("Mercy Ma", environment.getProperty("my.name"));
    }

    @EtcdPropertySource(
            key = "test.json",
            factory = JsonPropertySourceFactory.class)
    static class Config {

    }

}
