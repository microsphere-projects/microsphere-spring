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
import java.net.URL;
import java.net.URLConnection;

import static io.microsphere.util.ClassLoaderUtils.getClassResource;
import static org.junit.Assert.assertEquals;
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

        this.writable.setConnectTimeout(timeout);
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

        this.writable.setReadTimeout(timeout);
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
    }

    @Test
    public void testGetHeaderFields() {
    }

    @Test
    public void testGetHeaderFieldInt() {
    }

    @Test
    public void testGetHeaderFieldLong() {
    }

    @Test
    public void testGetHeaderFieldDate() {
    }

    @Test
    public void testGetHeaderFieldKey() {
    }

    @Test
    public void testTestGetHeaderField() {
    }

    @Test
    public void testGetContent() {
    }

    @Test
    public void testTestGetContent() {
    }

    @Test
    public void testGetPermission() {
    }

    @Test
    public void testGetInputStream() {
    }

    @Test
    public void testGetOutputStream() {
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testSetDoInput() {
    }

    @Test
    public void testGetDoInput() {
    }

    @Test
    public void testSetDoOutput() {
    }

    @Test
    public void testGetDoOutput() {
    }

    @Test
    public void testSetAllowUserInteraction() {
    }

    @Test
    public void testGetAllowUserInteraction() {
    }

    @Test
    public void testSetDefaultAllowUserInteraction() {
    }

    @Test
    public void testGetDefaultAllowUserInteraction() {
    }

    @Test
    public void testSetUseCaches() {
    }

    @Test
    public void testGetUseCaches() {
    }

    @Test
    public void testSetIfModifiedSince() {
    }

    @Test
    public void testGetIfModifiedSince() {
    }

    @Test
    public void testGetDefaultUseCaches() {
    }

    @Test
    public void testSetDefaultUseCaches() {
    }

    @Test
    public void testSetRequestProperty() {
    }

    @Test
    public void testAddRequestProperty() {
    }

    @Test
    public void testGetRequestProperty() {
    }

    @Test
    public void testGetRequestProperties() {
    }

    @Test
    public void testSetDefaultRequestProperty() {
    }

    @Test
    public void testGetDefaultRequestProperty() {
    }

    @Test
    public void testSetContentHandlerFactory() {
    }

    @Test
    public void testGetContentHandler() {
    }

    @Test
    public void testGuessContentTypeFromName() {
    }

    @Test
    public void testGuessContentTypeFromStream() {
    }

    @Test
    public void testTestSetDoOutput() {
    }

    @Test
    public void testTestGetHeaderField1() {
    }

    @Test
    public void testTestGetHeaderFields() {
    }

    @Test
    public void testTestGetHeaderFieldKey() {
    }

    @Test
    public void testTestGetHeaderField2() {
    }

    @Test
    public void testTestGetInputStream() {
    }

    @Test
    public void testTestGetOutputStream() {
    }

    @Test
    public void testTestSetRequestProperty() {
    }

    @Test
    public void testTestAddRequestProperty() {
    }

    @Test
    public void testTestGetRequestProperty() {
    }

    @Test
    public void testTestGetRequestProperties() {
    }

    @Test
    public void testConnect() {
    }

    @Test
    public void testToString1() {
    }
}