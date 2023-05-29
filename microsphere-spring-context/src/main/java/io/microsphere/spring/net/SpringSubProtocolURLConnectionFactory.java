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
import org.springframework.util.Assert;

import java.net.URL;
import java.util.List;

/**
 * Spring Abstract {@link SubProtocolURLConnectionFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class SpringSubProtocolURLConnectionFactory implements SubProtocolURLConnectionFactory {

    private static final int SUB_PROTOCOL_INDEX = 0;

    @Override
    public final boolean supports(URL url, List<String> subProtocols) {
        String actualSubProtocol = getActualSubProtocol(subProtocols);
        String subProtocol = getSubProtocol();
        Assert.notNull(subProtocol, "The 'subProtocol' argument must not be null");
        return subProtocol.equals(actualSubProtocol);
    }

    /**
     * Get the sub-protocol
     *
     * @return non-null;
     */
    protected abstract String getSubProtocol();

    private String getActualSubProtocol(List<String> subProtocols) {
        return subProtocols.get(SUB_PROTOCOL_INDEX);
    }
}
