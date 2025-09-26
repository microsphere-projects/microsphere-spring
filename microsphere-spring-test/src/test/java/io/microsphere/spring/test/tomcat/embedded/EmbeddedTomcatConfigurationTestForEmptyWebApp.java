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
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link EmbeddedTomcatConfiguration} Test with default attributes
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedTomcatConfiguration
 * @since 1.0.0
 */
@EmbeddedTomcatConfiguration(
        port = 0,
        contextPath = "/",
        docBase = "classpath:/webapps/empty-app",
        classes = {
                HashMap.class
        },
        locations = "classpath:webapps/empty-app/WEB-INF/context.xml"
)
class EmbeddedTomcatConfigurationTestForEmptyWebApp {

    @Autowired(required = false)
    private WebApplicationContext wac;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private HashMap hashMap;

    @Autowired
    @Qualifier("testString")
    private String testString;

    @Test
    void test() {
        assertNull(this.wac);

        assertNotNull(this.context);

        assertNotNull(this.hashMap);

        assertEquals("test", this.testString);
    }
}
