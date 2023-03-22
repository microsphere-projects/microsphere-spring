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
package io.github.microsphere.spring.net;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * {@link SpringProtocolURLStreamHandler} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringResourceURLConnectionFactory
 * @see SpringEnvironmentURLConnectionFactory
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpringProtocolURLStreamHandler.class, SpringTestURLConnectionFactory.class})
@TestPropertySource(properties = {"microsphere.net.a=1", "microsphere.net.b=2", "microsphere.net.c=3",})
public class SpringProtocolURLStreamHandlerTest {

    @Test
    public void testSpringResourceURLConnectionFactory() throws Throwable {
        assertContent(new URL("spring:resource:classpath://enable-configuration-bean-binding.properties"));
    }

    @Test
    public void testSpringEnvironmentURLConnectionFactory() throws Throwable {
        assertContent(new URL("spring:env:property-sources://microsphere.net/text/properties"));
    }

    @Test
    public void testSpringDelegatingBeanURLConnectionFactory() throws Throwable {
        assertContent(new URL("spring:test://test"));
    }

    private void assertContent(URL url) throws Throwable {
        String content = null;
        try (InputStream inputStream = url.openStream()) {
            content = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        }
        assertNotNull(content);
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
