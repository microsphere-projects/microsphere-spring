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

import io.github.microsphere.net.SubProtocolURLConnectionFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static io.github.microsphere.constants.PathConstants.SLASH;
import static io.github.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.github.microsphere.net.URLUtils.resolveAuthority;
import static io.github.microsphere.net.URLUtils.resolvePath;

/**
 * Spring {@link Resource} {@link SubProtocolURLConnectionFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringResourceURLConnectionFactory extends SpringSubProtocolURLConnectionFactory {

    private static final String RESOURCE_SUB_PROTOCOL = "resource";
    private static final int RESOURCE_SCHEME_INDEX = 1;

    private final ResourceLoader resourceLoader;

    public SpringResourceURLConnectionFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
        Resource resource = findResource(url, subProtocols);
        return new SpringResourceURLConnection(url, resource);
    }

    protected Resource findResource(URL url, List<String> subProtocols) throws IOException {
        String location = buildLocation(url, subProtocols);
        ResourceLoader resourceLoader = this.resourceLoader;
        Resource resource = resourceLoader.getResource(location);
        if (resource == null) {
            throw new IOException("The Spring resource can't be found from the URL : " + url);
        }
        return resource;
    }

    @Override
    protected String getSubProtocol() {
        return RESOURCE_SUB_PROTOCOL;
    }

    private String buildLocation(URL url, List<String> subProtocols) {
        String scheme = getResourceScheme(subProtocols);
        String authority = resolveAuthority(url);
        String path = resolvePath(url);
        return scheme + COLON_CHAR + SLASH + authority + path;
    }

    private String getResourceScheme(List<String> subProtocols) {
        return subProtocols.get(RESOURCE_SCHEME_INDEX);
    }
}
