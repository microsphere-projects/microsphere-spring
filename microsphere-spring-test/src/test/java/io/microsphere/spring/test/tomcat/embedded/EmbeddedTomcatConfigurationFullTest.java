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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;

import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.DEFAULT_WEB_XML;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.NAMING;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.SILENT;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.USE_TEST_CLASSPATH;
import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature.WEB_APP_DEFAULTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link EmbeddedTomcatConfiguration} Test for "/webapps/empty-app" enabling  with cases as following:
 * <ul>
 *     <li>{@link TestPropertySource} with resource properties</li>
 *     <li>Resolve the placeholders for String type attributes</li>
 *     <li>The Context's document base directory can be found via {@link EmbeddedTomcatConfiguration#docBase()}</li>
 *     <li>The alternative deployment descriptor can't be found via {@link EmbeddedTomcatConfiguration#alternativeWebXml()}
 *     with placeholders</li>
 *     <li>No configuration class can be registered via {@link EmbeddedTomcatConfiguration#classes()}</li>
 *     <li>The context locations can be loaded via {@link EmbeddedTomcatConfiguration#locations()}</li>
 *     <li>The {@link ApplicationContextInitializer} can be initialized via {@link EmbeddedTomcatConfiguration#initializers()}</li>
 *     <li>The {@link EmbeddedTomcatConfiguration#inheritLocations()} is disabled</li>
 *     <li>The {@link EmbeddedTomcatConfiguration#inheritInitializers()} is disabled</li>
 *     <li>All features are enabled</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedTomcatConfiguration
 * @since 1.0.0
 */
@EmbeddedTomcatConfiguration(
        port = 0,
        contextPath = "${contextPath}",
        docBase = "${docBase}",
        alternativeWebXml = "${alternativeWebXml}",
        classes = {},
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
class EmbeddedTomcatConfigurationFullTest implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ApplicationContext context;

    private static ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("testString")
    private String testString;

    @Test
    void test() {
        assertNotNull(this.wac);

        assertNotNull(this.context);

        assertNotNull(applicationContext);

        assertNotSame(this.wac, this.context);

        assertSame(this.context, applicationContext);

        assertEquals("test", this.testString);
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
