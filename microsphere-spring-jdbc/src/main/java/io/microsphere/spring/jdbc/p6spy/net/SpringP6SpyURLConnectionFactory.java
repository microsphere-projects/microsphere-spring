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
package io.microsphere.spring.jdbc.p6spy.net;

import io.microsphere.net.SubProtocolURLConnectionFactory;
import io.microsphere.spring.net.SpringEnvironmentURLConnectionFactory;
import io.microsphere.spring.net.SpringSubProtocolURLConnectionFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Spring P6Spy {@link SubProtocolURLConnectionFactory} class delegates {@link SpringEnvironmentURLConnectionFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringEnvironmentURLConnectionFactory
 * @since 1.0.0
 */
public class SpringP6SpyURLConnectionFactory extends SpringSubProtocolURLConnectionFactory {

    @Override
    protected String getSubProtocol() {
        return "p6spy";
    }

    @Override
    public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
        String prefix = getPrefix(url);
        URL targetURL = new URL("spring:env:property-sources://" + prefix);
        return targetURL.openConnection();
    }

    private String getPrefix(URL url) {
        String authority = url.getAuthority();
        return StringUtils.hasText(authority) ? authority : "microsphere.jdbc.p6spy";
    }
}
