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
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.core.env.EnumerablePropertySource;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.emptyMap;

/**
 * Zookeeper PropertySource
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ZookeeperPropertySource extends EnumerablePropertySource<Map<String, Object>> implements CuratorWatcher {

    private final String configPath;

    private final CuratorFramework client;

    private final boolean reloadable;

    private volatile Map<String, Object> cache;

    public ZookeeperPropertySource(String configPath, CuratorFramework client, boolean reloadable) {
        super("zookeeper://" + configPath);
        this.configPath = configPath;
        this.client = client;
        this.reloadable = reloadable;
        load();
        if (reloadable) {
            watch();
        }
    }

    protected void load() {
        if (!reloadable && cache != null) {
            return;
        }
        Map<String, Object> localCache = new HashMap<>();
        try {
            ConfigEntity configEntity = getConfigEntity(configPath);
            String contentType = configEntity.getHeader().getContentType();
            String body = configEntity.getBody();

            switch (contentType) {
                case "text/properties":
                    Properties properties = new Properties();
                    properties.load(new StringReader(body));
                    localCache.putAll((Map) properties);
                    break;
                case "text/json":
                    // TODO
                    break;
                case "text/xml":
                    // TODO
                    break;
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Can't load Zookeeper Config Entry!", e);
        }
        this.cache = localCache;
    }

    private void watch() {
        try {
            this.client.getData().usingWatcher(this).forPath(configPath);
        } catch (Exception e) {
            throw new IllegalStateException("Can't watch the config path : " + configPath, e);
        }
    }

    private void clear() {
        Map<String, Object> cache = this.cache;
        if (cache != null) {
            cache.clear();
            synchronized (cache) {
                this.cache = emptyMap();
            }
        }
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        Watcher.Event.EventType eventType = event.getType();
        switch (eventType) {
            case NodeCreated:
            case NodeDataChanged:
                load();
                break;
            case NodeDeleted:
                clear();
        }
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
