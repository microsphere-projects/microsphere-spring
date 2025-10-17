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

import org.apache.catalina.Context;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

/**
 * The annotation to startup the Embedded Zookeeper server for Spring integration testing.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Context
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@RunWith(SpringRunner.class)
@TestExecutionListeners(
        listeners = EmbeddedZookeeperServerTestExecutionListener.class,
        mergeMode = MERGE_WITH_DEFAULTS
)
public @interface EmbeddedZookeeperServer {

    /**
     * The port of Zookeeper Server
     *
     * @return the default value of port : 2181
     */
    int port() default 2181;

    /**
     * The resource location of the Zookeeper Server
     * <p>
     * The value supports two types of locations:
     * <ul>
     *     <li>File System, e.g: /tmp/catalina </li>
     *     <li>Spring Resource location, e.g: classpath:/catalina, file:/temp/catalina</li>
     * </ul>
     *
     * @return the default value of basedir : The "java.io.tmpdir" system property (the directory where Java temporary
     * directory) where a directory named Zookeeper Server.$PORT will be created. $PORT is the value configured via
     */
    String dataDir() default "${java.io.tmpdir}/test-zookeeper-${port}";

    /**
     * The tick time of Zookeeper Server
     *
     * @return the default value of tick time : 2000
     */
    long tickTime() default 2000L;
}