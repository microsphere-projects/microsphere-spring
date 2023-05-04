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

import io.github.microsphere.spring.config.annotation.EnableConfigAttributes;
import io.github.microsphere.spring.config.annotation.EnableConfigPropertySourceLoader;
import io.github.microsphere.spring.config.zookeeper.env.ZookeeperPropertySource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

import static io.github.microsphere.constants.PathConstants.SLASH_CHAR;

/**
 * {@link EnableZookeeperConfig} {@link PropertySource} Loader to load the Zookeeper Configuration:
 * <ul>
 *     <li>Create a CuratorFramework client based on the @EnableZookeeperConfig meta information, connection string, and root path</li>
 *     <li>Traverse all PropertySource child nodes according to the root path rootPath</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class EnableZookeeperConfigPropertySourceLoader extends EnableConfigPropertySourceLoader<EnableZookeeperConfig> {

    @Override
    protected PropertySource<?> loadPropertySource(EnableConfigAttributes<EnableZookeeperConfig> enableConfigAttributes,
                                                   String propertySourceName, AnnotationMetadata metadata) {
        String connectString = enableConfigAttributes.getString("connectString");
        String rootPath = enableConfigAttributes.getString("rootPath");

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new RetryForever(300))
                .build();

        client.start();

        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);
        try {
            if (client.checkExists().forPath(rootPath) == null) {
                client.create().forPath(rootPath);
            }
            List<String> configBasePaths = client.getChildren().forPath(rootPath);
            for (String configBasePath : configBasePaths) {
                configBasePath = rootPath + SLASH_CHAR + configBasePath;
                ZookeeperPropertySource zookeeperPropertySource = new ZookeeperPropertySource(configBasePath, client);
                compositePropertySource.addPropertySource(zookeeperPropertySource);
            }
        } catch (Exception e) {
            throw new BeanCreationException("@EnableZookeeperConfig bean can't load the PropertySource[name :" + propertySourceName + "]", e);
        }

        return compositePropertySource;
    }
}
