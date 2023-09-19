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
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.watch.WatchEvent;
import io.microsphere.spring.config.context.annotation.PropertySourceExtensionLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;

/**
 * {@link EtcdPropertySource} {@link PropertySource} Loader to load the etcd Configuration:
 * <ul>
 *     <li>Create a CuratorFramework client based on the @EnableEtcdConfig meta information, connection string, and root path</li>
 *     <li>Traverse all PropertySource child nodes according to the root path rootPath</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class EtcdPropertySourceLoader extends PropertySourceExtensionLoader<EtcdPropertySource, EtcdPropertySourceAttributes> {

    private static final Map<String, Client> clientsCache;

    static {
        clientsCache = new HashMap<>();
        addShutdownHookCallback(new Runnable() {
            @Override
            public void run() {
                // Close clients
                close(clientsCache.values());
                // Clear clients cache when JVM is shutdown
                clientsCache.clear();
            }
        });
    }

    @Override
    protected Resource[] resolveResources(EtcdPropertySourceAttributes etcdPropertySourceAttributes,
                                          String propertySourceName, String resourceValue) throws Throwable {

        Client client = getClient(etcdPropertySourceAttributes);

        KV kv = client.getKVClient();
        ByteSequence key = toByteSequence(resourceValue, etcdPropertySourceAttributes);
        CompletableFuture<GetResponse> future = kv.get(key);
        GetResponse getResponse = future.get();
        List<KeyValue> keyValues = getResponse.getKvs();

        int size = keyValues.size();
        if (size < 1) {
            return null;
        }

        Resource[] resources = new Resource[size];

        for (int i = 0; i < size; i++) {
            KeyValue keyValue = keyValues.get(i);
            ByteSequence value = keyValue.getValue();
            resources[i] = new ByteArrayResource(value.getBytes());
        }

        return resources;
    }

    @Override
    protected void configureAutoRefreshedResources(EtcdPropertySourceAttributes etcdPropertySourceAttributes,
                                                   List<Resource> resourcesList, Comparator<Resource> resourceComparator, PropertySourceFactory factory, CompositePropertySource propertySource) throws Throwable {
        Client client = getClient(etcdPropertySourceAttributes);
        String propertySourceName = propertySource.getName();
        Watch watchClient = client.getWatchClient();
        String[] keyValues = etcdPropertySourceAttributes.getKeys();
        for (String keyValue : keyValues) {
            ByteSequence key = toByteSequence(keyValue, etcdPropertySourceAttributes);
            watchClient.watch(key, response -> {
                List<WatchEvent> watchEvents = response.getEvents();
                watchEvents.forEach(watchEvent -> {
                    WatchEvent.EventType eventType = watchEvent.getEventType();
                    if (WatchEvent.EventType.PUT.equals(eventType)) {
                        onConfigModified(watchEvent, etcdPropertySourceAttributes, propertySourceName);
                    }
                });
            });
        }

    }

    public void onConfigModified(WatchEvent watchEvent,
                                 EtcdPropertySourceAttributes etcdPropertySourceAttributes,
                                 String compositePropertySourceName) {
        // etcd config has been modified
        KeyValue keyValue = watchEvent.getKeyValue();
        ByteSequence key = keyValue.getKey();
        ByteSequence value = keyValue.getValue();
        ConfigurableEnvironment environment = getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        String encoding = etcdPropertySourceAttributes.getEncoding();
        Resource resource = new ByteArrayResource(value.getBytes());
        EncodedResource encodedResource = new EncodedResource(resource, encoding);
        PropertySourceFactory factory = createPropertySourceFactory(etcdPropertySourceAttributes);
        try {
            String propertySourceName = compositePropertySourceName + "-" + new String(key.getBytes(), encoding);
            PropertySource propertySource = factory.createPropertySource(propertySourceName, encodedResource);
            propertySources.addFirst(propertySource);
        } catch (IOException e) {
            logger.error("TODO message", e);
        }
    }

    private ByteSequence toByteSequence(String value, EtcdPropertySourceAttributes etcdPropertySourceAttribute) throws UnsupportedEncodingException {
        String encoding = etcdPropertySourceAttribute.getEncoding();
        return ByteSequence.from(value.getBytes(encoding));
    }

    private Client getClient(EtcdPropertySourceAttributes etcdPropertySourceAttributes) {
        String key = etcdPropertySourceAttributes.getName();
        return clientsCache.computeIfAbsent(key, k -> {
            ClientBuilder clientBuilder = Client.builder();
            String target = etcdPropertySourceAttributes.getTarget();
            if (StringUtils.hasText(target)) {
                // clientBuilder.target(target);
            } else {
                clientBuilder.endpoints(etcdPropertySourceAttributes.getEndpoints());
            }
            // TODO support more settings
            return clientBuilder.build();
        });
    }

    private static void close(Collection<Client> clients) {
        for (Client client : clients) {
            close(client);
        }
    }

    private static void close(Client client) {
        if (client != null) {
            client.close();
        }
    }

}
