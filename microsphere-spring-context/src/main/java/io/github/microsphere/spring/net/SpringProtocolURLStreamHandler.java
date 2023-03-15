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
import io.github.microsphere.net.SubProtocolURLConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;

import static io.github.microsphere.net.URLUtils.registerURLStreamHandler;

/**
 * The Spring {@link URLStreamHandler} component supports supports the "spring" sub-protocols,
 * like "spring:{sub-protocol}:{ext-1}: ... :{ext-n}://...",
 * {sub-protocol} is required, each between {ext-1} to {ext-n} is the optional extension part.
 * for instance, "spring:resource:classpath://abc.properties",
 * <ul>
 *     <li>{sub-protocol} : "resource"</li>
 *     <li>{ext-1} : "classpath"</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringProtocolURLStreamHandler extends ExtendableProtocolURLStreamHandler implements InitializingBean, ResourceLoaderAware {

    public static final String PROTOCOL = "spring";

    private volatile ResourceLoader resourceLoader;

    public SpringProtocolURLStreamHandler() {
        super(PROTOCOL);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.init();
    }

    @Override
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories) {
        factories.add(new SpringResourceURLConnectionFactory(getResourceLoader()));
    }

    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        List<String> subProtocols = resolveSubProtocols(url);
        int size = subProtocols.size();
        if (size < 1) {
            throw new MalformedURLException("The Spring Protocol URLStreamHandler must contain the sub-protocol part , like 'spring:{sub-protocol}:...'");
        }
        return super.openConnection(url, proxy);
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
    }
}
