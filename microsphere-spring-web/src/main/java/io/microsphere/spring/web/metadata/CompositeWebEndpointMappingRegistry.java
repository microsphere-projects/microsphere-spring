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

package io.microsphere.spring.web.metadata;

import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.Collection;
import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;

/**
 * Composite {@link WebEndpointMappingRegistry}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FilteringWebEndpointMappingRegistry
 * @see WebEndpointMappingRegistry
 * @since 1.0.0
 */
public class CompositeWebEndpointMappingRegistry extends FilteringWebEndpointMappingRegistry implements SmartInitializingSingleton {

    private List<WebEndpointMappingRegistry> registries = newLinkedList();

    @Override
    protected boolean doRegister(WebEndpointMapping webEndpointMapping) {
        boolean registered = false;
        for (WebEndpointMappingRegistry registry : registries) {
            registered |= registry.register(webEndpointMapping);
        }
        return registered;
    }

    @Override
    public Collection<WebEndpointMapping> getWebEndpointMappings() {
        return this.registries.stream()
                .flatMap(registry -> registry.getWebEndpointMappings().stream())
                .toList();
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<WebEndpointMappingRegistry> registries = getSortedBeans(this.beanFactory, WebEndpointMappingRegistry.class);
        for (WebEndpointMappingRegistry registry : registries) {
            if (registry != this) {
                this.registries.add(registry);
            }
        }
    }
}
