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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
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

    @BeforeClass
    public static void init() throws Exception {
        EnableZookeeperConfig annotation =
                EnableZookeeperConfigTest.class.getAnnotation(EnableZookeeperConfig.class);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(annotation.connectString())
                .retryPolicy(new RetryForever(300))
                .build();

        client.start();

        String rootPath = annotation.rootPath();

        // 创建根路径，如果不存在
        markDir(client, rootPath);

        // 添加模拟数据
        mockConfigEntity(client, rootPath, "config1");

    }

    private static void mockConfigEntity(CuratorFramework client, String rootPath, String configBasePath) throws Exception {
        String propertySourcePath = rootPath + "/" + configBasePath;

        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = patternResolver.getResources("classpath:/META-INF/zookeeper/*.json");

        // 创建 PropertySource 路径，如果不存在
        markDir(client, propertySourcePath);

        for (Resource resource : resources) {
            // my.name.json
            String propertyName = resource.getFilename();
            String propertyPath = propertySourcePath + "/" + propertyName;
            byte[] data = StreamUtils.copyToByteArray(resource.getInputStream());
            if (client.checkExists().forPath(propertyPath) != null) {
                client.delete().forPath(propertyPath);
            }
            client.create().forPath(propertyPath, data);
        }

    }

    private static void markDir(CuratorFramework client, String path) throws Exception {
        if (client.checkExists().forPath(path) == null) {
            client.create().forPath(path);
        }
    }

    @Autowired
    private Environment environment;

    @Test
    public void test() {
        assertEquals("mercyblitz", environment.getProperty("my.name.json"));
    }

    @EnableZookeeperConfig
    static class Config {

    }

}
