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
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.PropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.net.URL;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.net.URLUtils.ofURL;
import static io.microsphere.spring.net.SpringPropertySourcesURLConnectionAdapter.DEFAULT_MEDIA_TYPE;
import static io.microsphere.spring.net.SpringPropertySourcesURLConnectionAdapter.getJavaType;
import static io.microsphere.spring.net.SpringPropertySourcesURLConnectionAdapter.getMediaType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.springframework.util.MimeTypeUtils.TEXT_HTML;

/**
 * {@link SpringPropertySourcesURLConnectionAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringPropertySourcesURLConnectionAdapter
 * @since 1.0.0
 */
public class SpringPropertySourcesURLConnectionAdapterTest {

    @Test
    public void testConvert() {
        URL url = ofURL("http://test.com");
        MockEnvironment environment = new MockEnvironment();
        PropertySources propertySources = environment.getPropertySources();
        ConfigurableConversionService conversionService = environment.getConversionService();
        SpringPropertySourcesURLConnectionAdapter adapter = new SpringPropertySourcesURLConnectionAdapter(url, propertySources, conversionService);
        Object value = adapter.convert("test", String.class, String.class);
        assertEquals("test", value);

        assertThrows(UnsupportedOperationException.class, () -> adapter.convert("test", String.class, getClass()));
    }

    @Test
    public void testGetMediaType() {
        assertSame(DEFAULT_MEDIA_TYPE, getMediaType(""));
        assertSame(DEFAULT_MEDIA_TYPE, getMediaType(" "));
        assertSame(DEFAULT_MEDIA_TYPE, getMediaType("/"));
        assertEquals(TEXT_HTML, getMediaType("/text/html"));
        assertEquals(TEXT_HTML, getMediaType("text/html"));
    }

    @Test
    public void testGetJavaType() {
        assertSame(String.class, getJavaType("text"));
        assertSame(Properties.class, getJavaType("properties"));
        assertSame(Map.class, getJavaType("map"));
        assertThrows(UnsupportedOperationException.class, () -> getJavaType("unknown"));
    }
}