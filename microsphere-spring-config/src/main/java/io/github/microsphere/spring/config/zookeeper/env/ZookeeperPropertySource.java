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
package io.github.microsphere.spring.config.zookeeper.env;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.microsphere.spring.config.zookeeper.metadata.ConfigEntity;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.core.env.EnumerablePropertySource;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zookeeper PropertySource
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ZookeeperPropertySource extends EnumerablePropertySource<Map<String, Object>> {

    private final String configBasePath;

    private final CuratorFramework client;

    private Map<String, Object> cache;

    public ZookeeperPropertySource(String configBasePath, CuratorFramework client) {
        super("zookeeper://" + configBasePath);
        this.configBasePath = configBasePath;
        this.client = client;
        initCache();
    }

    private void initCache() {
        // TODO Async
        // list the children under the config base path
        Map<String, Object> localCache = new HashMap<>();
        try {
            List<String> propertyNamePaths = client.getChildren().forPath(this.configBasePath);
            for (String propertyNamePath : propertyNamePaths) {

                String configEntityPath = configBasePath + "/" + propertyNamePath;
                ConfigEntity configEntity = getConfigEntity(configEntityPath);
                String contentType = configEntity.getHeader().getContentType();
                String body = configEntity.getBody();

                switch (contentType) {
                    case "text/plain":
                        localCache.put(propertyNamePath, body);
                        break;
                    case "text/properties":
                        Properties properties = new Properties();
                        try {
                            properties.load(new StringReader(body));
                            localCache.putAll((Map) properties);
                        } catch (IOException e) {
                            // TODO
                        }
                        break;
                    case "text/json":
                        // TODO
                        break;
                    case "text/xml":
                        // TODO
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.cache = localCache;
    }

    @Override
    public Object getProperty(String name) {
        return cache.get(name);
    }

    /**
     * @return
     */
    @Override
    public String[] getPropertyNames() {
        return this.cache.keySet().toArray(new String[0]);
    }

    private ConfigEntity getConfigEntity(String propertyNamePath) throws Exception {
        byte[] bytes = this.client.getData().forPath(propertyNamePath);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(bytes, ConfigEntity.class);
    }
}
