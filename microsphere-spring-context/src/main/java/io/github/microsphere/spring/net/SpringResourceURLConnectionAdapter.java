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

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;

/**
 * The internal class of {@link URLConnection} adapter is based on the Spring {@link Resource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class SpringResourceURLConnectionAdapter extends URLConnection {

    private final Resource resource;

    private final WritableResource writableResource;

    private MultiValueMap<String, String> requestProperties;

    private MultiValueMap<String, String> headers;

    protected SpringResourceURLConnectionAdapter(URL rawURL, Resource resource) {
        super(rawURL);
        this.resource = resource;
        this.writableResource = resource instanceof WritableResource ? (WritableResource) resource : null;
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        if (writableResource == null && dooutput) {
            throw new UnsupportedOperationException("The resource does not support output!");
        }
        super.setDoOutput(dooutput);
    }

    @Override
    public String getHeaderField(String name) {
        MultiValueMap<String, String> headers = this.headers;
        return headers == null ? null : headers.getFirst(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        MultiValueMap<String, String> headers = this.headers;
        return headers == null ? emptyMap() : unmodifiableMap(headers);
    }

    @Override
    public String getHeaderFieldKey(int n) {
        Map.Entry<String, List<String>> entry = getHeaderEntry(n);
        return entry == null ? null : entry.getKey();
    }

    @Override
    public String getHeaderField(int n) {
        Map.Entry<String, List<String>> entry = getHeaderEntry(n);
        if (entry == null) {
            return null;
        }
        List<String> value = entry.getValue();
        return value != null && value.isEmpty() ? value.get(0) : null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return resource.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (writableResource == null) {
            throw new UnsupportedOperationException("The resource does not support output!");
        }
        return writableResource.getOutputStream();
    }

    @Override
    public void setRequestProperty(String key, String value) {
        MultiValueMap<String, String> requestProperties = doGetRequestProperties();
        requestProperties.put(key, Arrays.asList(value));
    }

    @Override
    public void addRequestProperty(String key, String value) {
        MultiValueMap<String, String> requestProperties = doGetRequestProperties();
        requestProperties.add(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        MultiValueMap<String, String> requestProperties = this.requestProperties;
        return requestProperties == null ? null : requestProperties.getFirst(key);
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return unmodifiableMap(doGetRequestProperties());
    }

    @Override
    public void connect() throws IOException {
        super.connected = true;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + getURL();
    }

    protected Map.Entry<String, List<String>> getHeaderEntry(int n) {
        Set<Map.Entry<String, List<String>>> entries = getHeaderEntries();
        Iterator<Map.Entry<String, List<String>>> iterator = entries.iterator();
        Map.Entry<String, List<String>> entry = null;
        for (int i = 0; i < n & iterator.hasNext(); i++) {
            entry = iterator.next();
        }
        return entry;
    }

    protected Set<Map.Entry<String, List<String>>> getHeaderEntries() {
        MultiValueMap<String, String> headers = this.headers;
        return headers == null ? emptySet() : headers.entrySet();
    }

    protected MultiValueMap<String, String> getHeaders() {
        MultiValueMap<String, String> headers = this.headers;
        if (headers == null) {
            headers = new LinkedMultiValueMap<>();
        }
        return headers;
    }

    protected MultiValueMap<String, String> doGetRequestProperties() {
        MultiValueMap<String, String> requestProperties = this.requestProperties;
        if (requestProperties == null) {
            requestProperties = new LinkedMultiValueMap<>();
            this.requestProperties = requestProperties;
        }
        return requestProperties;
    }
}
