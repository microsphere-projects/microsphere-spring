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
package io.github.microsphere.spring.config.zookeeper.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.microsphere.net.URLUtils;
import io.github.microsphere.spring.config.zookeeper.metadata.ConfigEntity;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
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

import static org.junit.Assert.assertEquals;

/**
 * {@link EnableZookeeperConfig} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@EnableZookeeperConfig
@ContextConfiguration(classes = {
        EnableZookeeperConfigTest.class,
        EnableZookeeperConfigTest.Config.class
})
public class EnableZookeeperConfigTest {

    private static CuratorFramework client;

    @BeforeClass
    public static void init() throws Exception {
        EnableZookeeperConfig annotation =
                EnableZookeeperConfigTest.class.getAnnotation(EnableZookeeperConfig.class);
        client = CuratorFrameworkFactory.builder()
                .connectString(annotation.connectString())
                .retryPolicy(new RetryForever(300))
                .build();

        client.start();

        String rootPath = annotation.rootPath();

        // 创建根路径，如果不存在
        resolveDir(rootPath);

        // 添加模拟数据
        mockConfig(rootPath);

    }

    @AfterClass
    public static void destroy() {
        client.close();
    }

    private static void mockConfig(String rootPath) throws Exception {

        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = patternResolver.getResources("classpath:/META-INF/zookeeper/*.json");

        // 创建 PropertySource 路径，如果不存在
        resolveDir(rootPath);

        for (Resource resource : resources) {
            // test.json
            String fileName = resource.getFilename();
            String configPath = rootPath + "/" + fileName;
            byte[] data = StreamUtils.copyToByteArray(resource.getInputStream());
            writeConfig(configPath, data);
        }
    }

    private static void writeConfig(String path, ConfigEntity configEntity) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = objectMapper.writeValueAsBytes(configEntity);
        writeConfig(path, data);
    }

    private static void writeConfig(String path, byte[] data) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            client.create().forPath(path, data);
        } else {
            client.setData().forPath(path, data);
        }
    }

    private static void resolveDir(String path) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            client.create().forPath(path);
        } else {
            deleteNode(path);
        }
    }

    private static void deleteNode(String path) throws Exception {
        for (String childPath : client.getChildren().forPath(path)) {
            deleteNode(URLUtils.buildURI(path, childPath));
        }
        client.delete().forPath(path);
    }

    @Autowired
    private Environment environment;

    @Test
    public void test() throws Exception {
        assertEquals("mercyblitz", environment.getProperty("my.name"));

        ConfigEntity configEntity = new ConfigEntity();
        ConfigEntity.Header header = new ConfigEntity.Header();
        header.setContentType("text/properties");
        configEntity.setHeader(header);
        configEntity.setBody("my.name: Mercy Ma");

        writeConfig("/configs/test.json", configEntity);
        Thread.sleep(10 * 1000);
        assertEquals("Mercy Ma", environment.getProperty("my.name"));

    }

    @EnableZookeeperConfig
    static class Config {

    }

}
