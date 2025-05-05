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

import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
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
public class SpringResourceURLConnectionAdapterTest extends AbstractSpringResourceURLConnectionTest {

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

}