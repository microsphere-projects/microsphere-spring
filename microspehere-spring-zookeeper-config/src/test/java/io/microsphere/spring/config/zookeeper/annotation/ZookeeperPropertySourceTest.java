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
package io.microsphere.spring.config.zookeeper.annotation;

import io.microsphere.net.URLUtils;
import io.microsphere.spring.config.env.support.JsonPropertySourceFactory;
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

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * {@link ZookeeperPropertySource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ZookeeperPropertySourceTest.class,
        ZookeeperPropertySourceTest.Config.class
})
public class ZookeeperPropertySourceTest {

    private static CuratorFramework client;

    @Autowired
    private Environment environment;

    @BeforeClass
    public static void init() throws Exception {
        ZookeeperPropertySource annotation =
                ZookeeperPropertySourceTest.Config.class.getAnnotation(ZookeeperPropertySource.class);
        client = CuratorFrameworkFactory.builder()
                .connectString(annotation.connectString())
                .retryPolicy(new RetryForever(300))
                .build();

        client.start();

        String rootPath = "/configs";

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

    @Test
    public void test() throws Exception {
        assertEquals("mercyblitz", environment.getProperty("my.name"));

        writeConfig("/configs/test.json", "my.name: Mercy Ma".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(1 * 100);
        assertEquals("Mercy Ma", environment.getProperty("my.name"));
    }

    @ZookeeperPropertySource(
            connectString = "127.0.0.1:2181",
            value = "/configs/test.json",
            factory = JsonPropertySourceFactory.class)
    static class Config {

    }

}
