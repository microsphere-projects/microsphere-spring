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

import io.microsphere.annotation.Nullable;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactories;
import static java.util.Collections.emptyList;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * The smart {@link WebEndpointMappingFactory} class based on Spring's {@link WebEndpointMappingFactory} SPI
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SmartWebEndpointMappingFactory extends AbstractWebEndpointMappingFactory<Object> {

    private static final Class<WebEndpointMappingFactory> FACTORY_CLASS = WebEndpointMappingFactory.class;

    private final Map<Class<?>, List<WebEndpointMappingFactory>> delegates;

    public SmartWebEndpointMappingFactory(@Nullable ConfigurableListableBeanFactory beanFactory) {
        this.delegates = loadDelegates(beanFactory);
    }

    private Map<Class<?>, List<WebEndpointMappingFactory>> loadDelegates(@Nullable ConfigurableListableBeanFactory beanFactory) {

        List<WebEndpointMappingFactory> factories = doLoadFactories(beanFactory);
        Collection<WebEndpointMappingFactory> factoryBeans = getFactoryBeans(beanFactory);

        int size = size(factories) + size(factoryBeans);

        Map<Class<?>, List<WebEndpointMappingFactory>> delegates = new HashMap<>(size);

        initDelegates(factories, delegates);
        initDelegates(factoryBeans, delegates);

        return delegates;
    }

    private Collection<WebEndpointMappingFactory> getFactoryBeans(ConfigurableListableBeanFactory beanFactory) {
        return beanFactory == null ? emptyList() : beanFactory.getBeansOfType(FACTORY_CLASS).values();
    }

    private void initDelegates(Collection<WebEndpointMappingFactory> factories,
                               Map<Class<?>, List<WebEndpointMappingFactory>> delegates) {
        for (WebEndpointMappingFactory factory : factories) {
            Class<?> sourceType = factory.getSourceType();
            List<WebEndpointMappingFactory> factoriesList = delegates.computeIfAbsent(sourceType, t -> newLinkedList());
            factoriesList.add(factory);
            sort(factoriesList);
        }
    }

    private List<WebEndpointMappingFactory> doLoadFactories(ConfigurableListableBeanFactory beanFactory) {
        return loadFactories(beanFactory, FACTORY_CLASS);
    }

    @Override
    protected WebEndpointMapping<?> doCreate(Object endpoint) throws Throwable {
        Class<?> sourceType = endpoint.getClass();
        List<WebEndpointMappingFactory> factories = delegates.get(sourceType);
        int size = size(factories);
        if (size < 1) {
            return null;
        }
        WebEndpointMapping<Object> result = null;
        for (int i = 0; i < size; i++) {
            WebEndpointMappingFactory factory = factories.get(i);
            if (factory.supports(endpoint)) {
                Optional<WebEndpointMapping<Object>> optionalResult = factory.create(endpoint);
                if (optionalResult.isPresent()) {
                    result = optionalResult.get();
                }
            }
        }
        return result;
    }
}
