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

package io.microsphere.spring.test.tomcat.embedded;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.DEFAULT_WEB_XML;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.NAMING;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.SILENT;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.USE_TEST_CLASSPATH;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.WEB_APP_DEFAULTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * {@link EmbeddedTomcatConfiguration} Test with full features
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedTomcatConfiguration
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@EmbeddedTomcatConfiguration(
        port = 0,
        contextPath = "${contextPath}",
        docBase = "${docBase}",
        classes = {
                HashMap.class
        },
        locations = "classpath:webapps/empty-app/WEB-INF/context.xml",
        initializers = EmbeddedTomcatConfigurationFullTest.class,
        inheritLocations = false,
        inheritInitializers = false,
        features = {
                NAMING,
                DEFAULT_WEB_XML,
                WEB_APP_DEFAULTS,
                USE_TEST_CLASSPATH,
                SILENT
        }
)
@TestPropertySource(locations = "classpath:config/test.properties")
public class EmbeddedTomcatConfigurationFullTest implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ApplicationContext context;

    private static ConfigurableApplicationContext applicationContext;

    @Autowired
    private HashMap hashMap;

    @Autowired
    @Qualifier("testString")
    private String testString;

    @Test
    public void test() {
        assertNotNull(this.wac);

        assertNotNull(this.context);

        assertNotNull(applicationContext);

        assertNotSame(this.wac, this.context);

        assertSame(this.context, applicationContext);

        assertNotNull(this.hashMap);

        assertEquals("test", this.testString);
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
