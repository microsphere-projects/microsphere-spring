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

import io.github.microsphere.net.ExtendableProtocolURLStreamHandler;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;

import static io.github.microsphere.constants.PathConstants.SLASH;
import static io.github.microsphere.constants.SymbolConstants.COLON_CHAR;
import static io.github.microsphere.net.URLUtils.registerURLStreamHandler;

/**
 * The Spring {@link URLStreamHandler} supports
 * the sub-protocols like "spring:{sub-protocol}:{type}:/{resource-path}", for example,
 * "spring:resource:classpath://abc.properties"
 *
 *
 * <ul>
 *     <li>{sub-protocol}: {@link Resource resource}</li>
 *     <li>{type} is "classpath"</li>
 *     <li>{resource-path} : "abc.properties"</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringProtocolURLStreamHandler extends ExtendableProtocolURLStreamHandler implements ResourceLoaderAware {

    public static final String PROTOCOL = "spring";

    private volatile ResourceLoader resourceLoader;

    public SpringProtocolURLStreamHandler() {
        super(PROTOCOL);
    }

    @Override
    protected URLConnection openConnection(URL rawURL) throws IOException {
        List<String> subProtocols = resolveSubProtocols(rawURL);
        int size = subProtocols.size();
        if (size < 1) {
            throw new MalformedURLException("The Spring Protocol URLStreamHandler must contain the sub-protocol part , like 'spring:{sub-protocol}:...'");
        }

        String subProtocol = subProtocols.get(0);
        if ("resource".equals(subProtocol) && size > 1) {
            Resource resource = findResource(rawURL, subProtocols);
            return new SpringResourceURLConnection(rawURL, resource);
        }
        // TODO support the more type

        return null;
    }

    protected Resource findResource(URL rawURL, List<String> subProtocols) throws IOException {
        String location = buildLocation(rawURL, subProtocols);
        ResourceLoader resourceLoader = getResourceLoader();
        Resource resource = resourceLoader.getResource(location);
        if (resource == null) {
            throw new IOException("The Spring resource can't be found from the URL : " + rawURL);
        }
        return resource;
    }

    private String buildLocation(URL rawURL, List<String> subProtocols) {
        String prefix = subProtocols.get(1);
        String authority = resolveAuthority(rawURL);
        String path = resolvePath(rawURL);
        return prefix + COLON_CHAR + SLASH + authority + path;
    }

    public ResourceLoader getResourceLoader() {
        ResourceLoader resourceLoader = this.resourceLoader;
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }
        return resourceLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        // register self
        registerURLStreamHandler(this);
    }
}
