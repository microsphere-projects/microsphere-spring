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
import org.springframework.beans.factory.ListableBeanFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;

/**
 * Spring delegating Beans of {@link SubProtocolURLConnectionFactory}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringDelegatingBeanProtocolURLConnectionFactory implements SubProtocolURLConnectionFactory {

    private final ListableBeanFactory beanFactory;

    public SpringDelegatingBeanProtocolURLConnectionFactory(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public boolean supports(URL url, List<String> subProtocols) {
        return selectDelegateIndex(url, subProtocols) > -1;
    }

    @Override
    public URLConnection create(URL url, List<String> subProtocols, Proxy proxy) throws IOException {
        List<SubProtocolURLConnectionFactory> delegatingBeans = getDelegatingBeans();
        int index = selectDelegateIndex(url, subProtocols, delegatingBeans);
        SubProtocolURLConnectionFactory delegatingBean = delegatingBeans.get(index);
        return delegatingBean.create(url, subProtocols, proxy);
    }

    private int selectDelegateIndex(URL url, List<String> subProtocols) {
        List<SubProtocolURLConnectionFactory> delegatingBeans = getDelegatingBeans();
        return selectDelegateIndex(url, subProtocols, delegatingBeans);
    }

    private int selectDelegateIndex(URL url, List<String> subProtocols, List<SubProtocolURLConnectionFactory> factories) {
        int index = -1;
        int size = factories.size();
        for (int i = 0; i < size; i++) {
            SubProtocolURLConnectionFactory factory = factories.get(i);
            if (factory.supports(url, subProtocols)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private List<SubProtocolURLConnectionFactory> getDelegatingBeans() {
        return getSortedBeans(beanFactory, SubProtocolURLConnectionFactory.class);
    }
}
