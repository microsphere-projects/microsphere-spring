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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * {@link EmbeddedTomcatConfiguration} Test for "/webapps/context-loader-app" with cases as following:
 * <ul>
 *     <li>{@link TestPropertySource} with inlined properties</li>
 *     <li>Resolve the placeholders for String type attributes</li>
 *     <li>The Context's document base directory can be found via {@link EmbeddedTomcatConfiguration#docBase()}</li>
 *     <li>The alternative deployment descriptor can't be found via {@link EmbeddedTomcatConfiguration#alternativeWebXml()}</li>
 *     <li>The configuration classes can be registered via {@link EmbeddedTomcatConfiguration#classes()}</li>
 * </ul>
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
        alternativeWebXml = "classpath:/not-found/web.xml"
)
@TestPropertySource(properties = {
        "contextPath=/",
        "docBase=classpath:/webapps/context-loader-app"
})
public class EmbeddedTomcatConfigurationContextLoaderWebAppTest {

    @Autowired(required = false)
    private WebApplicationContext wac;

    @Autowired
    private ApplicationContext context;

    @Value("${contextPath}")
    private String contextPath;

    @Value("${docBase}")
    private String docBase;

    @Autowired
    private Environment environment;

    @Test
    public void test() {
        assertNotNull(this.wac);

        assertNotNull(this.context);

        assertNotSame(this.wac, this.context);

        assertEquals("/", this.contextPath);

        assertEquals("classpath:/webapps/context-loader-app", docBase);
    }
}
