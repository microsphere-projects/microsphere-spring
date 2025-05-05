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
import java.security.AllPermission;
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

    private URL notFoundURL;

    private Resource readonlyResource;

    private Resource writableResource;

    private Resource notFoundResource;

    private SpringResourceURLConnectionAdapter writable;

    private SpringResourceURLConnectionAdapter readonly;

    private SpringResourceURLConnectionAdapter notFound;

    @Before
    public void before() throws Throwable {
        this.readonlyURL = getClassResource(SpringVersion.class);
        this.readonlyResource = new UrlResource(this.readonlyURL);
        this.readonly = new SpringResourceURLConnectionAdapter(this.readonlyURL, this.readonlyResource);

        this.writableURL = getURL("classpath:META-INF/data/temp.txt");
        this.writableResource = new FileSystemResource(this.writableURL.getPath());
        assertTrue(this.writableResource.exists());
        this.writable = new SpringResourceURLConnectionAdapter(this.writableURL, this.writableResource);

        this.notFoundURL = getURL("file:///not-found");
        this.notFoundResource = new UrlResource(this.notFoundURL);
        this.notFound = new SpringResourceURLConnectionAdapter(this.notFoundURL, this.notFoundResource);

    }

    @Test
    public void testConnectTimeout() {
        testConnectTimeout(this.readonly);
        testConnectTimeout(this.writable);
        testConnectTimeout(this.notFound);
    }


    @Test
    public void testReadTimeout() {
        testReadTimeout(this.readonly);
        testReadTimeout(this.writable);
        testReadTimeout(this.notFound);
    }

    @Test
    public void testGetURL() {
        assertSame(this.readonlyURL, this.readonly.getURL());
        assertSame(this.writableURL, this.writable.getURL());
        assertSame(this.notFoundURL, this.notFound.getURL());
    }

    @Test
    public void testGetContentLength() throws IOException {
        assertEquals(this.readonlyResource.contentLength(), this.readonly.getContentLength());
        assertEquals(this.writableResource.contentLength(), this.writable.getContentLength());
    }

    @Test
    public void testGetContentLengthOnNotFound() {
        assertEquals(-1L, this.notFound.getContentLength());
    }

    @Test
    public void testGetContentLengthLong() throws IOException {
        assertEquals(this.readonlyResource.contentLength(), this.readonly.getContentLengthLong());
        assertEquals(this.writableResource.contentLength(), this.writable.getContentLengthLong());
    }

    @Test
    public void testGetContentLengthLongOnNotFound() {
        assertEquals(-1L, this.notFound.getContentLengthLong());
    }

    @Test
    public void testGetContentType() {
        assertNull(this.readonly.getContentType());
        assertEquals("text/plain", this.writable.getContentType());
        assertNull(this.notFound.getContentType());
    }

    @Test
    public void testGetContentEncoding() {
        assertNull(this.readonly.getContentEncoding());
        assertNull(this.writable.getContentEncoding());
        assertNull(this.notFound.getContentEncoding());
    }

    @Test
    public void testGetExpiration() {
        assertEquals(0, this.readonly.getExpiration());
        assertEquals(0, this.writable.getExpiration());
        assertEquals(0, this.notFound.getExpiration());
    }

    @Test
    public void testGetDate() {
        assertEquals(0, this.readonly.getDate());
        assertEquals(0, this.writable.getDate());
        assertEquals(0, this.notFound.getDate());
    }

    @Test
    public void testGetLastModified() throws IOException {
        assertEquals(this.readonlyResource.lastModified(), this.readonly.getLastModified());
        assertEquals(this.writableResource.lastModified(), this.writable.getLastModified());
    }

    @Test
    public void testGetLastModifiedOnNotFound() {
        assertEquals(0L, this.notFound.getLastModified());
    }

    @Test
    public void testGetHeaderField() {
        testGetHeaderFieldByName(this.readonly);
        testGetHeaderFieldByName(this.writable);
        testGetHeaderFieldByName(this.notFound);
    }

    @Test
    public void testGetHeaderFields() {
        testGetHeaderFields(this.readonly);
        testGetHeaderFields(this.writable);
        testGetHeaderFields(this.notFound);
    }

    @Test
    public void testGetHeaderFieldInt() {
        testGetHeaderFieldInt(this.readonly);
        testGetHeaderFieldInt(this.writable);
        testGetHeaderFieldInt(this.notFound);
    }

    @Test
    public void testGetHeaderFieldLong() {
        testGetHeaderFieldLong(this.readonly);
        testGetHeaderFieldLong(this.writable);
        testGetHeaderFieldLong(this.notFound);
    }

    @Test
    public void testGetHeaderFieldDate() {
        testGetHeaderFieldDate(this.readonly);
        testGetHeaderFieldDate(this.writable);
        testGetHeaderFieldDate(this.notFound);
    }

    @Test
    public void testGetHeaderFieldKey() {
        testGetHeaderFieldKey(this.readonly);
        testGetHeaderFieldKey(this.writable);
        testGetHeaderFieldKey(this.notFound);
    }

    @Test
    public void testGetContent() throws IOException {
        assertNotNull(this.writable.getContent());
    }

    @Test(expected = IOException.class)
    public void testGetContentOnIOException() throws IOException {
        this.readonly.getContent();
    }

    @Test(expected = IOException.class)
    public void testGetContentOnNotFoundException() throws IOException {
        this.notFound.getContent();
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
    public void testGetPermission() throws IOException {
        testGetPermission(this.readonly);
        testGetPermission(this.writable);
        testGetPermission(this.notFound);
    }

    @Test
    public void testGetInputStream() throws IOException {
        testGetInputStream(this.readonly);
        testGetInputStream(this.writable);
    }

    @Test(expected = IOException.class)
    public void testGetInputStreamOnIOException() throws IOException {
        this.notFound.getInputStream();
    }

    void testGetPermission(URLConnection urlConnection) throws IOException {
        assertEquals(new AllPermission(), urlConnection.getPermission());
    }

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

    @Test
    public void testGetHeaderFieldByIndex() {
        testGetHeaderFieldByIndex(this.readonly);
        testGetHeaderFieldByIndex(this.writable);
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
        this.readonly.setDoOutput(false);
        assertFalse(this.readonly.getDoOutput());
        this.readonly.setDoOutput(true);
    }

    @Test
    public void testAllowUserInteraction() {
        testAllowUserInteraction(this.readonly);
        testAllowUserInteraction(this.writable);
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
        testUseCaches(this.notFound);
    }

    @Test
    public void testDefaultUseCaches() {
        testDefaultUseCaches(this.readonly);
        testDefaultUseCaches(this.writable);
        testDefaultUseCaches(this.notFound);
    }

    @Test
    public void testIfModifiedSince() throws IOException {
        testIfModifiedSince(this.readonly);
        testIfModifiedSince(this.writable);
        testIfModifiedSince(this.notFound);
    }

    @Test
    public void testRequestProperty() {
        testRequestProperty(this.readonly);
        testRequestProperty(this.writable);
        testRequestProperty(this.notFound);
    }

    @Test
    public void testConnect() throws IOException {
        testConnect(this.readonly);
        testConnect(this.writable);
        testConnect(this.notFound);
    }

    @Test
    public void testGetHeaderEntryOnOutOfRange() {
        testGetHeaderEntryOnOutOfRange(this.readonly);
        testGetHeaderEntryOnOutOfRange(this.writable);
        testGetHeaderEntryOnOutOfRange(this.notFound);
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

    void testGetHeaderEntryOnOutOfRange(SpringResourceURLConnectionAdapter adapter) {
        assertNull(adapter.getHeaderEntry(-1));
        assertNull(adapter.getHeaderEntry(1));
    }

    void testGetHeaderFieldInt(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "1";
        int expected = 1;

        assertEquals(0, adapter.getHeaderFieldInt(name, 0));
        adapter.addHeader(name, value);
        assertEquals(expected, adapter.getHeaderFieldInt(name, 0));
    }

    void testGetHeaderFieldLong(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "1";
        int expected = 1;

        assertEquals(0, adapter.getHeaderFieldLong(name, 0));
        adapter.addHeader(name, value);
        assertEquals(expected, adapter.getHeaderFieldLong(name, 0));
    }

    void testGetHeaderFieldDate(SpringResourceURLConnectionAdapter adapter) {
        String name = "name";
        String value = "Sat, 12 Aug 1995 13:30:00 GMT+0430";

        assertEquals(0, adapter.getHeaderFieldDate(name, 0));
        adapter.addHeader(name, value);
        assertEquals(Date.parse(value), adapter.getHeaderFieldDate(name, 0));
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

        assertNull(adapter.getHeaderFieldKey(-1));
        assertNull(adapter.getHeaderFieldKey(times));
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

        assertNull(adapter.getHeaderField(-1));
        assertNull(adapter.getHeaderField(times));
    }

    <T> void testHeader(SpringResourceURLConnectionAdapter adapter, String name, String value, Function<String, T> getFunction, T expected) {
        adapter.addHeader(name, value);
        assertEquals(expected, getFunction.apply(name));
    }

    void testConnect(SpringResourceURLConnectionAdapter adapter) throws IOException {
        try {
            assertFalse(adapter.isConnected());
            adapter.connect();
            assertTrue(adapter.isConnected());
        } finally {
            adapter.disconnect();
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
}