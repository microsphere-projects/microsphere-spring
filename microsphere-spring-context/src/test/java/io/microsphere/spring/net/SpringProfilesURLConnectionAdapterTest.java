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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.spring.net.SpringProfilesURLConnectionAdapter.getEncoding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * {@link SpringProfilesURLConnectionAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringProfilesURLConnectionAdapter
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        SpringProtocolURLStreamHandler.class,
        SpringProfilesURLConnectionAdapterTest.class
})
@TestPropertySource(
        properties = {
                "spring.profiles.active=dev,test"
        }
)
public class SpringProfilesURLConnectionAdapterTest {

    @Test
    public void test() throws IOException {
        assertProfiles("spring:env:profiles://", "default");
        assertProfiles("spring:env:profiles://default", "default");
        assertProfiles("spring:env:profiles://active", "dev", "test");
    }

    void assertProfiles(String urlString, String... expectedProfiles) throws IOException {
        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        assertTrue(urlConnection instanceof SpringProfilesURLConnectionAdapter);
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        String encoding = getEncoding(url);
        String profiles = copyToString(inputStream, encoding);
        assertEquals(arrayToCommaDelimitedString(expectedProfiles), profiles);
    }
}