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

import io.microsphere.net.SubProtocolURLConnectionFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Spring {@link Environment} {@link SubProtocolURLConnectionFactory}
 * <p>
 * The URL pattern : "spring:env:{type}://...",
 * <ul>
 *     <li>{type} : the source type(optional), {@link #PROPERTY_SOURCES_TYPE "property-sources"}(as default when type is absent)
 *     or {@link #PROFILES_TYPE "profiles"}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Environment
 * @see SubProtocolURLConnectionFactory
 * @since 1.0.0
 */
public class SpringEnvironmentURLConnectionFactory extends SpringSubProtocolURLConnectionFactory {

    public static final String PROPERTY_SOURCES_TYPE = "property-sources";

    public static final String PROFILES_TYPE = "profiles";

    private static final int TYPE_INDEX = 1;

    private final ConfigurableEnvironment environment;

    private final ConfigurableConversionService conversionService;

    public SpringEnvironmentURLConnectionFactory(ConfigurableEnvironment environment, ConfigurableConversionService conversionService) {
        this.environment = environment;
        this.conversionService = conversionService;
    }

    @Override
    protected String getSubProtocol() {
        return "env";
    }

    @Override
    public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
        String type = getType(subProtocols);
        URLConnection urlConnection = null;
        switch (type) {
            case PROFILES_TYPE:
                // TODO
                break;
            case PROPERTY_SOURCES_TYPE:
            default:
                urlConnection = new SpringPropertySourcesURLConnectionAdapter(url, environment.getPropertySources(), conversionService);
        }
        return urlConnection;
    }

    private String getType(List<String> subProtocols) {
        return subProtocols.get(TYPE_INDEX);
    }
}
