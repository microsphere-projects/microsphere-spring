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

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.ResourceUtils.getURL;
import static org.springframework.util.StringUtils.cleanPath;

/**
 * {@link TestExecutionListener} class for Embedded Apache ZooKeeper Server
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedZookeeperServer
 * @see TestExecutionListener
 * @since 1.0.0
 */
class EmbeddedZookeeperServerTestExecutionListener extends AbstractTestExecutionListener {

    static final String ZOOKEEP_SERVER_ATTRIBUTE_NAME = "microsphere:zookeeper-server";

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        EmbeddedZookeeperServer embeddedZookeeperServer = testClass.getAnnotation(EmbeddedZookeeperServer.class);
        // Resolve the metadata from @EmbeddedZookeeperServer
        int port = embeddedZookeeperServer.port();
        File dataDirectory = resolveDataDirectory(embeddedZookeeperServer, port);
        long tickTime = embeddedZookeeperServer.tickTime();
        // Startup the Embedded Zookeeper Server
        InstanceSpec instanceSpec = createInstanceSpec(port, dataDirectory, (int) tickTime);
        TestingServer testingServer = new TestingServer(instanceSpec, true);
        // Set the Embedded Zookeeper Server into attribute
        testContext.setAttribute(ZOOKEEP_SERVER_ATTRIBUTE_NAME, testingServer);
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        // Get the Embedded Zookeeper Server from attribute
        TestingServer testingServer = (TestingServer) testContext.getAttribute(ZOOKEEP_SERVER_ATTRIBUTE_NAME);
        testingServer.stop();
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 5;
    }

    private File resolveDataDirectory(EmbeddedZookeeperServer embeddedZookeeperServer, int port) throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> properties = new HashMap<>();
        properties.put("port", port);
        MapPropertySource localPropertySource = new MapPropertySource("_", properties);
        propertySources.addLast(localPropertySource);

        String dataDir = embeddedZookeeperServer.dataDir();
        String resolvedDataDir = cleanPath(environment.resolvePlaceholders(dataDir));
        URL url = getURL(resolvedDataDir);
        return new File(url.toURI());
    }

    private InstanceSpec createInstanceSpec(int port, File dataDirectory, int tickTime) {
        return new InstanceSpec(dataDirectory, port, -1, -1, true, -1, tickTime, -1, null, null);
    }
}
