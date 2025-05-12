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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.AllPermission;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.FieldUtils.setFieldValue;
import static java.lang.System.currentTimeMillis;
import static java.net.URLConnection.getDefaultAllowUserInteraction;
import static java.net.URLConnection.setDefaultAllowUserInteraction;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Abstract Test for {@link AbstractSpringResourceURLConnection}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSpringResourceURLConnection
 * @since 1.0.0
 */
public abstract class AbstractSpringResourceURLConnectionTest {

    @Test
    public abstract void testConnectTimeout();

    @Test
    public abstract void testReadTimeout();

    @Test
    public abstract void testGetURL();

    @Test
    public abstract void testGetContentLength() throws IOException;

    @Test
    public abstract void testGetContentLengthOnNotFound();

    @Test
    public abstract void testGetContentLengthLong() throws IOException;

    @Test
    public abstract void testGetContentLengthLongOnNotFound();

    @Test
    public abstract void testGetContentType();

    @Test
    public abstract void testGetContentEncoding();

    @Test
    public abstract void testGetExpiration();

    @Test
    public abstract void testGetDate();

    @Test
    public abstract void testGetLastModified() throws IOException;

    @Test
    public abstract void testGetLastModifiedOnNotFound();

    @Test
    public abstract void testGetHeaderField();

    @Test
    public abstract void testGetHeaderFields();

    @Test
    public abstract void testGetHeaderFieldInt();

    @Test
    public abstract void testGetHeaderFieldLong();

    @Test
    public abstract void testGetHeaderFieldDate();

    @Test
    public abstract void testGetHeaderFieldKey();

    @Test
    public abstract void testGetContent() throws IOException;

    @Test(expected = IOException.class)
    public abstract void testGetContentOnIOException() throws IOException;

    @Test(expected = IOException.class)
    public abstract void testGetContentOnNotFoundException() throws IOException;

    @Test
    public abstract void testGetContentWithClass() throws IOException;

    @Test(expected = IOException.class)
    public abstract void testGetContentWithClassOnIOException() throws IOException;

    @Test
    public abstract void testGetPermission() throws IOException;

    @Test
    public abstract void testGetInputStream() throws IOException;

    @Test(expected = IOException.class)
    public abstract void testGetInputStreamOnIOException() throws IOException;

    @Test
    public abstract void testGetHeaderFieldByIndex();

    @Test
    public abstract void testGetOutputStream() throws IOException;

    @Test(expected = IOException.class)
    public abstract void testGetOutputStreamOnIOException() throws IOException;

    @Test
    public abstract void testToString();

    @Test
    public abstract void testDoInput();

    @Test(expected = UnsupportedOperationException.class)
    public abstract void testDoInputOnIOException();

    @Test
    public abstract void testAllowUserInteraction();

    @Test
    public void testDefaultAllowUserInteraction() {
        boolean defaultAllowUserInteraction = getDefaultAllowUserInteraction();
        try {
            assertFalse(defaultAllowUserInteraction);
            setDefaultAllowUserInteraction(true);
            assertTrue(getDefaultAllowUserInteraction());
        } finally {
            setDefaultAllowUserInteraction(defaultAllowUserInteraction);
        }
    }

    @Test
    public abstract void testUseCaches();

    @Test
    public abstract void testDefaultUseCaches();

    @Test
    public abstract void testIfModifiedSince() throws IOException;

    @Test
    public abstract void testRequestProperty();

    @Test
    public abstract void testConnect() throws IOException;

    @Test
    public abstract void testGetHeaderEntryOnOutOfRange();

    void testGetHeaderFieldByName(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "value";
        testHeader(adapter, name, value, adapter::getHeaderField, value);
    }

    void testGetHeaderFields(SpringResourceURLConnectionAdapter adapter) {
        assertSame(emptyMap(), adapter.getHeaderFields());
        String name = "name";
        String value = "value";
        adapter.addHeader(name, value);
        assertEquals(singletonMap(name, asList(value)), adapter.getHeaderFields());
    }

    void testGetPermission(URLConnection urlConnection) throws IOException {
        assertEquals(new AllPermission(), urlConnection.getPermission());
    }

    void testConnectTimeout(URLConnection urlConnection) {
        int timeout = urlConnection.getConnectTimeout();
        try {
            assertEquals(0, urlConnection.getConnectTimeout());
            urlConnection.setConnectTimeout(100);
            assertEquals(100, urlConnection.getConnectTimeout());
        } finally {
            urlConnection.setConnectTimeout(timeout);
        }
    }

    void testReadTimeout(URLConnection urlConnection) {
        int timeout = urlConnection.getReadTimeout();
        try {
            assertEquals(0, urlConnection.getReadTimeout());
            urlConnection.setReadTimeout(100);
            assertEquals(100, urlConnection.getReadTimeout());
        } finally {
            urlConnection.setReadTimeout(timeout);
        }
    }

    void testGetInputStream(URLConnection urlConnection) throws IOException {
        try (InputStream inputStream = urlConnection.getInputStream()) {
            assertNotNull(inputStream);
        }
    }

    void testAllowUserInteraction(URLConnection urlConnection) {
        boolean defaultAllowUserInteraction = getDefaultAllowUserInteraction();
        try {
            assertEquals(defaultAllowUserInteraction, urlConnection.getAllowUserInteraction());
            urlConnection.setAllowUserInteraction(!defaultAllowUserInteraction);
            assertEquals(!defaultAllowUserInteraction, urlConnection.getAllowUserInteraction());
        } finally {
            urlConnection.setAllowUserInteraction(defaultAllowUserInteraction);
        }
    }

    void testUseCaches(URLConnection urlConnection) {
        boolean useCaches = urlConnection.getUseCaches();
        boolean defaultUseCaches = urlConnection.getDefaultUseCaches();
        try {
            assertEquals(defaultUseCaches, useCaches);
            urlConnection.setUseCaches(!defaultUseCaches);
            assertEquals(!defaultUseCaches, urlConnection.getUseCaches());
        } finally {
            urlConnection.setUseCaches(useCaches);
        }
    }

    void testDefaultUseCaches(URLConnection urlConnection) {
        boolean defaultUseCaches = urlConnection.getDefaultUseCaches();
        try {
            urlConnection.setDefaultUseCaches(!defaultUseCaches);
            assertEquals(!defaultUseCaches, urlConnection.getDefaultUseCaches());
        } finally {
            urlConnection.setUseCaches(defaultUseCaches);
        }
    }

    void testIfModifiedSince(URLConnection urlConnection) throws IOException {
        long ifModifiedSince = urlConnection.getIfModifiedSince();
        String connectedFieldName = "connected";
        boolean connected = getFieldValue(urlConnection, connectedFieldName, boolean.class);
        try {
            assertEquals(0, ifModifiedSince);
            long currentTimeMillis = currentTimeMillis();
            urlConnection.setIfModifiedSince(currentTimeMillis);
            assertEquals(currentTimeMillis, urlConnection.getIfModifiedSince());

            urlConnection.connect();
            urlConnection.setIfModifiedSince(currentTimeMillis);
        } catch (IllegalStateException e) {
            assertNotNull(e);
        } finally {
            setFieldValue(urlConnection, connectedFieldName, connected);
            urlConnection.setIfModifiedSince(ifModifiedSince);
        }
    }

    void testRequestProperty(URLConnection urlConnection) {
        Map<String, List<String>> requestProperties = urlConnection.getRequestProperties();
        assertTrue(requestProperties.isEmpty());
        String key = "test-key";
        String value = "test-value";

        urlConnection.setRequestProperty(key, value);

        requestProperties = urlConnection.getRequestProperties();
        assertEquals(1, requestProperties.size());
        assertTrue(requestProperties.containsKey(key));
        assertEquals(ofList(value), requestProperties.get(key));
        assertEquals(value, urlConnection.getRequestProperty(key));

        urlConnection.addRequestProperty(key, value);
        assertEquals(1, requestProperties.size());
        assertTrue(requestProperties.containsKey(key));
        assertEquals(ofList(value, value), requestProperties.get(key));
        assertEquals(value, urlConnection.getRequestProperty(key));
    }

    void testConnect(URLConnection urlConnection) throws IOException {
        String connectedFieldName = "connected";
        boolean connected = getFieldValue(urlConnection, connectedFieldName, boolean.class);
        try {
            assertFalse(connected);
            urlConnection.connect();
            assertTrue(getFieldValue(urlConnection, connectedFieldName, boolean.class));
        } finally {
            setFieldValue(urlConnection, connectedFieldName, connected);
        }
    }


    void testGetHeaderEntryOnOutOfRange(AbstractSpringResourceURLConnection urlConnection) {
        assertNull(urlConnection.getHeaderEntry(-1));
        assertNull(urlConnection.getHeaderEntry(1));
    }

    void testGetHeaderFieldInt(AbstractSpringResourceURLConnection urlConnection) {
        String name = "name";
        String value = "1";
        int expected = 1;

        assertEquals(0, urlConnection.getHeaderFieldInt(name, 0));
        urlConnection.addHeader(name, value);
        assertEquals(expected, urlConnection.getHeaderFieldInt(name, 0));
    }

    void testGetHeaderFieldLong(AbstractSpringResourceURLConnection urlConnection) {
        String name = "name";
        String value = "1";
        int expected = 1;

        assertEquals(0, urlConnection.getHeaderFieldLong(name, 0));
        urlConnection.addHeader(name, value);
        assertEquals(expected, urlConnection.getHeaderFieldLong(name, 0));
    }

    void testGetHeaderFieldDate(AbstractSpringResourceURLConnection urlConnection) {
        String name = "name";
        String value = "Sat, 12 Aug 1995 13:30:00 GMT+0430";

        assertEquals(0, urlConnection.getHeaderFieldDate(name, 0));
        urlConnection.addHeader(name, value);
        assertEquals(Date.parse(value), urlConnection.getHeaderFieldDate(name, 0));
    }

    void testGetHeaderFieldKey(AbstractSpringResourceURLConnection urlConnection) {
        int times = 9;
        for (int i = 0; i < times; i++) {
            String name = "name" + i;
            String value = "value" + i;
            urlConnection.addHeader(name, value);
        }

        for (int i = 0; i < times; i++) {
            assertEquals("name" + i, urlConnection.getHeaderFieldKey(i));
        }

        assertNull(urlConnection.getHeaderFieldKey(-1));
        assertNull(urlConnection.getHeaderFieldKey(times));
    }

    void testGetHeaderFieldByIndex(AbstractSpringResourceURLConnection urlConnection) {
        int times = 9;
        for (int i = 0; i < times; i++) {
            String name = "name" + i;
            String value = "value" + i;
            urlConnection.addHeader(name, value);
        }

        for (int i = 0; i < times; i++) {
            assertEquals("value" + i, urlConnection.getHeaderField(i));
        }

        assertNull(urlConnection.getHeaderField(-1));
        assertNull(urlConnection.getHeaderField(times));
    }

    <T> void testHeader(AbstractSpringResourceURLConnection urlConnection, String name, String value, Function<String, T> getFunction, T expected) {
        urlConnection.addHeader(name, value);
        assertEquals(expected, getFunction.apply(name));
    }

    void testConnect(AbstractSpringResourceURLConnection urlConnection) throws IOException {
        try {
            assertFalse(urlConnection.isConnected());
            urlConnection.connect();
            assertTrue(urlConnection.isConnected());
        } finally {
            urlConnection.disconnect();
        }
    }

}
