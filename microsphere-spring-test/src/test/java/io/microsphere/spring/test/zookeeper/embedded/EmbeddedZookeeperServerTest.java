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

package io.microsphere.spring.test.zookeeper.embedded;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.curator.framework.CuratorFrameworkFactory.newClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EmbeddedZookeeperServer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedZookeeperServer
 * @since 1.0.0
 */
@EmbeddedZookeeperServer
public class EmbeddedZookeeperServerTest {

    @Test
    void test() throws Exception {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curatorFramework = newClient("localhost:2181", retryPolicy);
        curatorFramework.start();

        String path = "/test";
        String payload = "testing...";
        Charset charset = UTF_8;
        curatorFramework.create().forPath(path, payload.getBytes(charset));
        byte[] bytes = curatorFramework.getData().forPath(path);
        assertEquals(payload, new String(bytes, charset));

        curatorFramework.delete().forPath(path);
        curatorFramework.close();
    }
}
