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

import io.github.microsphere.spring.config.zookeeper.env.ZookeeperPropertySource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;

/**
 * Zookeeper Configuration
 * 1. Create a CuratorFramework client based on the @EnableZookeeperConfig meta information, connection string, and root path
 * 2. Traverse all PropertySource child nodes according to the root path rootPath
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableZookeeperConfig
 * @see CuratorFramework
 * @see PropertySource
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
public class ZookeeperConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private MutablePropertySources propertySources;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableZookeeperConfig.class.getName());
        String connectString = (String) attributes.get("connectString");
        String rootPath = (String) attributes.get("rootPath");

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new RetryForever(300))
                .build();

        client.start();

        try {
            if (client.checkExists().forPath(rootPath) == null) {
                client.create().forPath(rootPath);
            }

            List<String> configBasePaths = client.getChildren().forPath(rootPath);
            for (String configBasePath : configBasePaths) {
                configBasePath = rootPath + "/" + configBasePath;
                ZookeeperPropertySource zookeeperPropertySource = new ZookeeperPropertySource(configBasePath, client);
                propertySources.addLast(zookeeperPropertySource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            this.propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
        }
    }
}
