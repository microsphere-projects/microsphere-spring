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
import org.springframework.core.SpringVersion;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static java.net.URLConnection.getDefaultAllowUserInteraction;
import static java.net.URLConnection.setDefaultAllowUserInteraction;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.MimeTypeUtils.TEXT_PLAIN_VALUE;
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
    public void setUp() throws Throwable {
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

    @Override
    public void testConnectTimeout() {
        testConnectTimeout(this.readonly);
        testConnectTimeout(this.writable);
        testConnectTimeout(this.notFound);
    }

    @Override
    public void testReadTimeout() {
        testReadTimeout(this.readonly);
        testReadTimeout(this.writable);
        testReadTimeout(this.notFound);
    }

    @Override
    public void testGetURL() {
        assertSame(this.readonlyURL, this.readonly.getURL());
        assertSame(this.writableURL, this.writable.getURL());
        assertSame(this.notFoundURL, this.notFound.getURL());
    }

    @Override
    public void testGetContentLength() throws IOException {
        assertEquals(this.readonlyResource.contentLength(), this.readonly.getContentLength());
        assertEquals(this.writableResource.contentLength(), this.writable.getContentLength());
    }

    @Override
    public void testGetContentLengthOnNotFound() {
        assertEquals(-1L, this.notFound.getContentLength());
    }

    @Override
    public void testGetContentLengthLong() throws IOException {
        assertEquals(this.readonlyResource.contentLength(), this.readonly.getContentLengthLong());
        assertEquals(this.writableResource.contentLength(), this.writable.getContentLengthLong());
    }

    @Override
    public void testGetContentLengthLongOnNotFound() {
        assertEquals(-1L, this.notFound.getContentLengthLong());
    }

    @Override
    public void testGetContentType() {
        assertNull(this.readonly.getContentType());
        assertEquals(TEXT_PLAIN_VALUE, this.writable.getContentType());
        assertNull(this.notFound.getContentType());
    }

    @Override
    public void testGetContentEncoding() {
        assertNull(this.readonly.getContentEncoding());
        assertNull(this.writable.getContentEncoding());
        assertNull(this.notFound.getContentEncoding());
    }

    @Override
    public void testGetExpiration() {
        assertEquals(0, this.readonly.getExpiration());
        assertEquals(0, this.writable.getExpiration());
        assertEquals(0, this.notFound.getExpiration());
    }

    @Override
    public void testGetDate() {
        assertEquals(0, this.readonly.getDate());
        assertEquals(0, this.writable.getDate());
        assertEquals(0, this.notFound.getDate());
    }

    @Override
    public void testGetLastModified() throws IOException {
        assertEquals(this.readonlyResource.lastModified(), this.readonly.getLastModified());
        assertEquals(this.writableResource.lastModified(), this.writable.getLastModified());
    }

    @Override
    public void testGetLastModifiedOnNotFound() {
        assertEquals(0L, this.notFound.getLastModified());
    }

    @Override
    public void testGetHeaderField() {
        testGetHeaderFieldByName(this.readonly);
        testGetHeaderFieldByName(this.writable);
        testGetHeaderFieldByName(this.notFound);
    }

    @Override
    public void testGetHeaderFields() {
        testGetHeaderFields(this.readonly);
        testGetHeaderFields(this.writable);
        testGetHeaderFields(this.notFound);
    }

    @Override
    public void testGetHeaderFieldInt() {
        testGetHeaderFieldInt(this.readonly);
        testGetHeaderFieldInt(this.writable);
        testGetHeaderFieldInt(this.notFound);
    }

    @Override
    public void testGetHeaderFieldLong() {
        testGetHeaderFieldLong(this.readonly);
        testGetHeaderFieldLong(this.writable);
        testGetHeaderFieldLong(this.notFound);
    }

    @Override
    public void testGetHeaderFieldDate() {
        testGetHeaderFieldDate(this.readonly);
        testGetHeaderFieldDate(this.writable);
        testGetHeaderFieldDate(this.notFound);
    }

    @Override
    public void testGetHeaderFieldKey() {
        testGetHeaderFieldKey(this.readonly);
        testGetHeaderFieldKey(this.writable);
        testGetHeaderFieldKey(this.notFound);
    }

    @Override
    public void testGetContent() throws IOException {
        assertNotNull(this.writable.getContent());
    }

    @Override
    public void testGetContentOnIOException() {
        assertThrows(IOException.class, this.readonly::getContent);
    }

    @Override
    public void testGetContentOnNotFoundException() {
        assertThrows(IOException.class, this.notFound::getContent);
    }

    @Override
    public void testGetContentWithClass() throws IOException {
        assertNotNull(this.writable.getContent(ofArray(InputStream.class)));
        assertNull(this.writable.getContent(ofArray(String.class)));
    }

    @Override
    public void testGetContentWithClassOnIOException() {
        assertThrows(IOException.class, () -> this.readonly.getContent(ofArray(InputStream.class)));
    }

    @Override
    public void testGetPermission() throws IOException {
        testGetPermission(this.readonly);
        testGetPermission(this.writable);
        testGetPermission(this.notFound);
    }

    @Override
    public void testGetInputStream() throws IOException {
        testGetInputStream(this.readonly);
        testGetInputStream(this.writable);
    }

    @Override
    public void testGetInputStreamOnIOException() {
        assertThrows(IOException.class, this.notFound::getInputStream);
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
        assertEquals(singletonMap(name, ofList(value)), adapter.getHeaderFields());
    }

    @Override
    public void testGetHeaderFieldByIndex() {
        testGetHeaderFieldByIndex(this.readonly);
        testGetHeaderFieldByIndex(this.writable);
    }

    @Override
    public void testGetOutputStream() throws IOException {
        try (OutputStream outputStream = this.writable.getOutputStream()) {
            assertNotNull(outputStream);
        }
    }

    @Override
    public void testGetOutputStreamOnIOException() {
        assertThrows(IOException.class, this.readonly::getOutputStream);
    }

    @Override
    public void testToString() {
        assertNotNull(this.readonly.toString());
        assertNotNull(this.writable.toString());
    }

    @Override
    public void testDoInput() {
        assertFalse(this.writable.getDoOutput());
        this.writable.setDoOutput(true);
        assertTrue(this.writable.getDoOutput());

        this.writable.setDoOutput(false);
        assertFalse(this.writable.getDoOutput());
    }

    @Override
    public void testDoInputOnIOException() {
        assertFalse(this.readonly.getDoOutput());
        this.readonly.setDoOutput(false);
        assertFalse(this.readonly.getDoOutput());
        assertThrows(UnsupportedOperationException.class, () -> this.readonly.setDoOutput(true));
    }

    @Override
    public void testAllowUserInteraction() {
        testAllowUserInteraction(this.readonly);
        testAllowUserInteraction(this.writable);
    }

    @Override
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

    @Override
    public void testUseCaches() {
        testUseCaches(this.readonly);
        testUseCaches(this.writable);
        testUseCaches(this.notFound);
    }

    @Override
    public void testDefaultUseCaches() {
        testDefaultUseCaches(this.readonly);
        testDefaultUseCaches(this.writable);
        testDefaultUseCaches(this.notFound);
    }

    @Override
    public void testIfModifiedSince() throws IOException {
        testIfModifiedSince(this.readonly);
        testIfModifiedSince(this.writable);
        testIfModifiedSince(this.notFound);
    }

    @Override
    public void testRequestProperty() {
        testRequestProperty(this.readonly);
        testRequestProperty(this.writable);
        testRequestProperty(this.notFound);
    }

    @Override
    public void testConnect() throws IOException {
        testConnect(this.readonly);
        testConnect(this.writable);
        testConnect(this.notFound);
    }

    @Override
    public void testGetHeaderEntryOnOutOfRange() {
        testGetHeaderEntryOnOutOfRange(this.readonly);
        testGetHeaderEntryOnOutOfRange(this.writable);
        testGetHeaderEntryOnOutOfRange(this.notFound);
    }

}