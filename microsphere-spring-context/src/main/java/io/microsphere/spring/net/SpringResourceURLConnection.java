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

import io.microsphere.net.DelegatingURLConnection;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Spring {@link Resource} {@link URLConnection}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Resource
 * @see ResourceLoader
 * @since 1.0.0
 */
public class SpringResourceURLConnection extends DelegatingURLConnection {

    private final Resource resource;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param rawURL   the raw {@link URL}
     * @param resource the {@link Resource}
     */
    public SpringResourceURLConnection(URL rawURL, Resource resource) throws IOException {
        super(getDelegate(rawURL, resource));
        this.resource = resource;
    }

    private static URLConnection getDelegate(URL rawURL, Resource resource) {
        URLConnection delegate = null;
        URL url = null;
        try {
            url = resource.getURL();
            delegate = url.openConnection();
        } catch (IOException e) {
            // The Spring Resource can't support the getURL() method
            delegate = new SpringResourceURLConnectionAdapter(rawURL, resource);
        }
        return delegate;
    }

    /**
     * Get the {@link Resource}
     *
     * @return the {@link Resource}
     */
    public Resource getResource() {
        return resource;
    }
}
