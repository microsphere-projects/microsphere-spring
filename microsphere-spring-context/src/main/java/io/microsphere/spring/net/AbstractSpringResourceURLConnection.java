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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static io.microsphere.collection.ListUtils.first;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * Abstract {@link URLConnection} based on Spring {@link Resource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see URLConnection
 * @see Resource
 * @since 1.0.0
 */
public class AbstractSpringResourceURLConnection extends URLConnection {

    protected final Resource resource;

    protected final WritableResource writableResource;

    private MultiValueMap<String, String> requestProperties;

    private MultiValueMap<String, String> headers;

    public AbstractSpringResourceURLConnection(@Nonnull URL url, @Nonnull Resource resource) {
        super(url);
        this.resource = resource;
        this.writableResource = resource instanceof WritableResource ? (WritableResource) resource : null;
    }

    @Override
    public String getHeaderField(String name) {
        MultiValueMap<String, String> headers = getHeaders();
        return headers.getFirst(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        MultiValueMap<String, String> headers = getHeaders();
        return headers.isEmpty() ? emptyMap() : unmodifiableMap(headers);
    }

    @Override
    public String getHeaderFieldKey(int n) {
        Entry<String, List<String>> entry = getHeaderEntry(n);
        return entry == null ? null : entry.getKey();
    }

    @Override
    public String getHeaderField(int n) {
        Entry<String, List<String>> entry = getHeaderEntry(n);
        if (entry == null) {
            return null;
        }
        List<String> value = entry.getValue();
        return first(value);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        MultiValueMap<String, String> requestProperties = doGetRequestProperties();
        List<String> values = new ArrayList<>(1);
        values.add(value);
        requestProperties.put(key, values);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        MultiValueMap<String, String> requestProperties = doGetRequestProperties();
        requestProperties.add(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        MultiValueMap<String, String> requestProperties = doGetRequestProperties();
        return requestProperties.getFirst(key);
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return unmodifiableMap(doGetRequestProperties());
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        if (writableResource == null && dooutput) {
            throw new UnsupportedOperationException("The resource does not support output!");
        }
        super.setDoOutput(dooutput);
    }

    @Override
    public void connect() throws IOException {
        super.connected = true;
    }

    public void disconnect() throws IOException {
        super.connected = false;
    }

    public boolean isConnected() {
        return super.connected;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "url=" + url +
                ", resource=" + resource +
                '}';
    }

    @Nonnull
    public Resource getResource() {
        return resource;
    }

    @Nullable
    public WritableResource getWritableResource() {
        return writableResource;
    }

    protected Entry<String, List<String>> getHeaderEntry(int n) {
        MultiValueMap<String, String> headers = getHeaders();
        if (n < 0 || n > headers.size() - 1) { // out of the index range
            return null;
        }
        Set<Entry<String, List<String>>> entries = headers.entrySet();
        Iterator<Entry<String, List<String>>> iterator = entries.iterator();
        Entry<String, List<String>> entry = null;
        for (int i = 0; i <= n & iterator.hasNext(); i++) {
            entry = iterator.next();
        }
        return entry;
    }

    protected MultiValueMap<String, String> getHeaders() {
        MultiValueMap<String, String> headers = this.headers;
        if (headers == null) {
            headers = new LinkedMultiValueMap<>();
            this.headers = headers;
        }
        return headers;
    }

    protected void addHeader(String name, String value) {
        MultiValueMap<String, String> headers = getHeaders();
        headers.add(name, value);
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
