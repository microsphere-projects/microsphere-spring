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

import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KV;
import io.microsphere.spring.config.context.annotation.ResourcePropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
public class EnableEtcdConfigPropertySourceLoader extends ResourcePropertySourceLoader<EtcdPropertySource, EtcdPropertySourceAttributes> {

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
    protected Resource[] getResources(EtcdPropertySourceAttributes etcdPropertySourceAttributes,
                                      String propertySourceName, String resourceLocation) throws Throwable {
        Resource[] resources = null;

        Client client = getClient(etcdPropertySourceAttributes);

        KV kv = client.getKVClient();
        String[] value = etcdPropertySourceAttributes.getValue();


        return resources;
    }

    private Client getClient(EtcdPropertySourceAttributes etcdPropertySourceAttributes) {
        String key = etcdPropertySourceAttributes.getName();
        return clientsCache.computeIfAbsent(key, k -> {
            ClientBuilder clientBuilder = Client.builder();
            String target = etcdPropertySourceAttributes.getTarget();
            if (StringUtils.hasText(target)) {
                clientBuilder.target(target);
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
