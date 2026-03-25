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
package io.microsphere.spring.net;

import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link SpringProtocolURLStreamHandler} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringResourceURLConnectionFactory
 * @see SpringEnvironmentURLConnectionFactory
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        SpringProtocolURLStreamHandler.class,
        SpringTestURLConnectionFactory.class
})
@TestPropertySource(properties = {
        "microsphere.net.a=1",
        "microsphere.net.b=2",
        "microsphere.net.c=3"
})
public class SpringProtocolURLStreamHandlerTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    @Test
    public void testSpringResourceURLConnectionFactory() throws Throwable {
        assertContent(new URL("spring:resource:classpath://enable-configuration-bean-binding.properties"));
    }

    @Test
    public void testSpringEnvironmentURLConnectionFactory() throws Throwable {
        assertContent(new URL("spring:env:property-sources://microsphere.net/text/properties"));
        URL url = new URL("spring:env:test://");
        assertNull(url.openConnection());
    }

    @Test
    public void testSpringDelegatingBeanURLConnectionFactory() throws Throwable {
        assertContent(new URL("spring:test://test"));
    }

    private void assertContent(URL url) throws Throwable {
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        try (InputStream inputStream = urlConnection.getInputStream()) {
            String content = copyToString(inputStream, defaultCharset());
            assertNotNull(content);
        }
    }
}

class SpringTestURLConnectionFactory extends SpringSubProtocolURLConnectionFactory {

    @Override
    protected String getSubProtocol() {
        return "test";
    }

    @Override
    public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("enable-configuration-bean-binding.properties");
        return resource.openConnection();
    }
}
