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


import org.junit.Before;
import org.junit.Test;
import org.springframework.core.SpringVersion;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.reflect.FieldUtils.setFieldValue;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
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
import static org.springframework.util.ResourceUtils.getURL;

/**
 * {@link SpringResourceURLConnectionAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringResourceURLConnectionAdapter
 * @since 1.0.0
 */
public class SpringResourceURLConnectionAdapterTest {

    private URL readonlyURL;

    private URL writableURL;

    private Resource readonlyResource;

    private Resource writableResource;

    private SpringResourceURLConnectionAdapter writable;

    private SpringResourceURLConnectionAdapter readonly;

    @Before
    public void before() throws Throwable {
        this.readonlyURL = getClassResource(SpringVersion.class);
        this.readonlyResource = new UrlResource(this.readonlyURL);
        this.readonly = new SpringResourceURLConnectionAdapter(this.readonlyURL, this.readonlyResource);

        this.writableURL = getURL("classpath:META-INF/data/temp.txt");
        this.writableResource = new FileSystemResource(this.writableURL.getPath());
        assertTrue(this.writableResource.exists());
        this.writable = new SpringResourceURLConnectionAdapter(this.writableURL, this.writableResource);
    }

    @Test
    public void testConnectTimeoutForReadonly() {
        testConnectTimeout(this.readonly);
    }

    @Test
    public void testConnectTimeoutForWritable() {
        testConnectTimeout(this.writable);
    }

    private void testConnectTimeout(URLConnection urlConnection) {
        int timeout = 0;
        assertEquals(timeout, urlConnection.getConnectTimeout());

        urlConnection.setConnectTimeout(timeout);
        assertEquals(timeout, urlConnection.getConnectTimeout());
    }

    @Test
    public void testReadTimeoutForReadonly() {
        testReadTimeout(this.readonly);
    }

    @Test
    public void testReadTimeoutForWritable() {
        testReadTimeout(this.writable);
    }

    private void testReadTimeout(URLConnection urlConnection) {
        int timeout = 0;
        assertEquals(timeout, urlConnection.getReadTimeout());

        urlConnection.setReadTimeout(timeout);
        assertEquals(timeout, urlConnection.getReadTimeout());
    }

    @Test
    public void testGetURL() {
        assertSame(this.readonlyURL, this.readonly.getURL());
        assertSame(this.writableURL, this.writable.getURL());
    }

    @Test
    public void testGetContentLength() throws IOException {
        assertEquals(this.readonlyResource.contentLength(), this.readonly.getContentLength());
        assertEquals(this.writableResource.contentLength(), this.writable.getContentLength());
    }

    @Test
    public void testGetContentLengthLong() throws IOException {
        assertEquals(this.readonlyResource.contentLength(), this.readonly.getContentLengthLong());
        assertEquals(this.writableResource.contentLength(), this.writable.getContentLengthLong());
    }

    @Test
    public void testGetContentType() {
        assertNull(this.readonly.getContentType());
        assertEquals("text/plain", this.writable.getContentType());
    }

    @Test
    public void testGetContentEncoding() {
        assertNull(this.readonly.getContentEncoding());
        assertNull(this.writable.getContentEncoding());
    }

    @Test
    public void testGetExpiration() {
        assertEquals(0, this.readonly.getExpiration());
        assertEquals(0, this.writable.getExpiration());
    }

    @Test
    public void testGetDate() {
        assertEquals(0, this.readonly.getDate());
        assertEquals(0, this.writable.getDate());
    }

    @Test
    public void testGetLastModified() throws IOException {
        assertEquals(this.readonlyResource.lastModified(), this.readonly.getLastModified());
        assertEquals(this.writableResource.lastModified(), this.writable.getLastModified());
    }

    @Test
    public void testGetHeaderField() {
        testGetHeaderFieldByName(this.readonly);
        testGetHeaderFieldByName(this.writable);
    }

    void testGetHeaderFieldByName(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "value";
        testHeader(adapter, name, value, adapter::getHeaderField, value);
    }

    @Test
    public void testGetHeaderFields() {
        testGetHeaderFields(this.readonly);
        testGetHeaderFields(this.writable);
    }

    void testGetHeaderFields(SpringResourceURLConnectionAdapter adapter) {
        assertSame(emptyMap(), adapter.getHeaderFields());
        String name = "name";
        String value = "value";
        adapter.addHeader(name, value);
        assertEquals(singletonMap(name, asList(value)), adapter.getHeaderFields());
    }

    @Test
    public void testGetHeaderFieldInt() {
        testGetHeaderFieldInt(this.readonly);
        testGetHeaderFieldInt(this.writable);
    }

    void testGetHeaderFieldInt(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "1";
        int expected = 1;

        assertEquals(0, adapter.getHeaderFieldInt(name, 0));
        adapter.addHeader(name, value);
        assertEquals(expected, adapter.getHeaderFieldInt(name, 0));
    }

    @Test
    public void testGetHeaderFieldLong() {
        testGetHeaderFieldLong(this.readonly);
        testGetHeaderFieldLong(this.writable);
    }

    void testGetHeaderFieldLong(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "1";
        int expected = 1;

        assertEquals(0, adapter.getHeaderFieldLong(name, 0));
        adapter.addHeader(name, value);
        assertEquals(expected, adapter.getHeaderFieldLong(name, 0));
    }

    @Test
    public void testGetHeaderFieldDate() {
        testGetHeaderFieldDate(this.readonly);
        testGetHeaderFieldDate(this.writable);
    }

    void testGetHeaderFieldDate(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "Sat, 12 Aug 1995 13:30:00 GMT+0430";

        assertEquals(0, adapter.getHeaderFieldDate(name, 0));
        adapter.addHeader(name, value);
        assertEquals(Date.parse(value), adapter.getHeaderFieldDate(name, 0));
    }

    @Test
    public void testGetHeaderFieldKey() {
        testGetHeaderFieldKey(this.readonly);
        testGetHeaderFieldKey(this.writable);
    }

    void testGetHeaderFieldKey(SpringResourceURLConnectionAdapter adapter) {
        int times = 9;
        for (int i = 0; i < times; i++) {
            String name = "name" + i;
            String value = "value" + i;
            adapter.addHeader(name, value);
        }

        for (int i = 0; i < times; i++) {
            assertEquals("name" + i, adapter.getHeaderFieldKey(i));
        }
    }

    @Test
    public void testGetHeaderFieldByIndex() {
        testGetHeaderFieldByIndex(this.readonly);
        testGetHeaderFieldByIndex(this.writable);
    }

    void testGetHeaderFieldByIndex(SpringResourceURLConnectionAdapter adapter) {
        int times = 9;
        for (int i = 0; i < times; i++) {
            String name = "name" + i;
            String value = "value" + i;
            adapter.addHeader(name, value);
        }

        for (int i = 0; i < times; i++) {
            assertEquals("value" + i, adapter.getHeaderField(i));
        }
    }

    <T> void testHeader(SpringResourceURLConnectionAdapter adapter, String name, String value, Function<String, T> getFunction, T expected) {
        adapter.addHeader(name, value);
        assertEquals(expected, getFunction.apply(name));
    }

    @Test
    public void testGetContent() throws IOException {
        assertNotNull(this.writable.getContent());
    }

    @Test(expected = IOException.class)
    public void testGetContentOnIOException() throws IOException {
        assertNotNull(this.readonly.getContent());
    }

    @Test
    public void testGetContentWithClass() throws IOException {
        assertNotNull(this.writable.getContent(ofArray(InputStream.class)));
        assertNull(this.writable.getContent(ofArray(String.class)));
    }

    @Test(expected = IOException.class)
    public void testGetContentWithClassOnIOException() throws IOException {
        this.readonly.getContent(ofArray(InputStream.class));
    }

    @Test
    public void testGetPermission() {
    }

    @Test
    public void testGetInputStream() throws IOException {
        testGetInputStream(this.readonly);
        testGetInputStream(this.writable);
    }

    void testGetInputStream(SpringResourceURLConnectionAdapter adapter) throws IOException {
        try (InputStream inputStream = adapter.getInputStream()) {
            assertNotNull(inputStream);
        }
    }

    @Test
    public void testGetOutputStream() throws IOException {
        try (OutputStream outputStream = this.writable.getOutputStream()) {
            assertNotNull(outputStream);
        }
    }

    @Test(expected = IOException.class)
    public void testGetOutputStreamOnIOException() throws IOException {
        this.readonly.getOutputStream();
    }

    @Test
    public void testToString() {
        assertNotNull(this.readonly.toString());
        assertNotNull(this.writable.toString());
    }

    @Test
    public void testDoInput() {
        assertFalse(this.writable.getDoOutput());
        this.writable.setDoOutput(true);
        assertTrue(this.writable.getDoOutput());

        this.writable.setDoOutput(false);
        assertFalse(this.writable.getDoOutput());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDoInputOnIOException() {
        assertFalse(this.readonly.getDoOutput());
        this.readonly.setDoOutput(true);
    }

    @Test
    public void testAllowUserInteraction() {
        testAllowUserInteraction(this.readonly);
        testAllowUserInteraction(this.writable);
    }

    void testAllowUserInteraction(SpringResourceURLConnectionAdapter adapter) {
        assertEquals(getDefaultAllowUserInteraction(), adapter.getAllowUserInteraction());
        adapter.setAllowUserInteraction(true);
        assertTrue(adapter.getAllowUserInteraction());
    }

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
    public void testUseCaches() {
        testUseCaches(this.readonly);
        testUseCaches(this.writable);
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

    @Test
    public void testDefaultUseCaches() {
        testDefaultUseCaches(this.readonly);
        testDefaultUseCaches(this.writable);
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

    @Test
    public void testIfModifiedSince() throws IOException {
        testIfModifiedSince(this.readonly);
        testIfModifiedSince(this.writable);
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

    @Test
    public void testRequestProperty() {
        testRequestProperty(this.readonly);
        testRequestProperty(this.writable);
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

    @Test
    public void testConnect() throws IOException {
        testConnect(this.readonly);
        testConnect(this.writable);
    }

    void testConnect(SpringResourceURLConnectionAdapter adapter) throws IOException {
        assertFalse(adapter.isConnected());
        adapter.connect();
        assertTrue(adapter.isConnected());
    }
}