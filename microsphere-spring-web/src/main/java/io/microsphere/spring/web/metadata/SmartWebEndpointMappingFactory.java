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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getDefaultClassLoader;
import static org.springframework.util.StringUtils.hasText;

/**
 * The smart {@link WebEndpointMappingFactory} class based on Spring's {@link WebEndpointMappingFactory} SPI
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SmartWebEndpointMappingFactory implements WebEndpointMappingFactory<Object> {

    private static final Class<WebEndpointMappingFactory> FACTORY_CLASS = WebEndpointMappingFactory.class;

    private final static Logger logger = LoggerFactory.getLogger(FACTORY_CLASS);

    private final Map<Class<?>, List<WebEndpointMappingFactory>> delegates;

    SmartWebEndpointMappingFactory() {
        this(null);
    }

    public SmartWebEndpointMappingFactory(@Nullable ConfigurableListableBeanFactory beanFactory) {
        this.delegates = loadDelegates(beanFactory);
    }

    private Map<Class<?>, List<WebEndpointMappingFactory>> loadDelegates(@Nullable ConfigurableListableBeanFactory beanFactory) {
        ClassLoader classLoader = getClassLoader(beanFactory);

        List<WebEndpointMappingFactory> factories = loadFactories(classLoader);
        Collection<WebEndpointMappingFactory> factoryBeans = getFactoryBeans(beanFactory);

        int size = factories.size() + factoryBeans.size();

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
            List<WebEndpointMappingFactory> factoriesList =
                    delegates.computeIfAbsent(sourceType, t -> new LinkedList<>());
            factoriesList.add(factory);
            AnnotationAwareOrderComparator.sort(factoriesList);
        }
    }

    private List<WebEndpointMappingFactory> loadFactories(ClassLoader classLoader) {
        List<String> factoryClassNames = loadFactoryNames(FACTORY_CLASS, classLoader);
        int size = factoryClassNames.size();
        if (size == 0) {
            return emptyList();
        }
        List<WebEndpointMappingFactory> factories = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String factoryClassName = factoryClassNames.get(i);
            if (hasText(factoryClassName)) {
                WebEndpointMappingFactory factory = createFactory(factoryClassName, classLoader);
                if (factory != null) {
                    factories.add(factory);
                }
            }
        }
        return factories;
    }

    private ClassLoader getClassLoader(ConfigurableListableBeanFactory beanFactory) {
        return beanFactory == null ? getDefaultClassLoader() : beanFactory.getBeanClassLoader();
    }

    private WebEndpointMappingFactory createFactory(String factoryClassName,
                                                    ClassLoader targetClassLoader) {
        WebEndpointMappingFactory factory = null;
        try {
            Class<?> factoryClass = targetClassLoader.loadClass(factoryClassName);
            factory = (WebEndpointMappingFactory) factoryClass.newInstance();
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("The factory class[name :'{}'] can't be instantiated!", factoryClassName);
            }
        }
        return factory;
    }

    @Override
    public Optional<WebEndpointMapping<?>> create(Object source) {
        Class<?> sourceType = source.getClass();
        List<WebEndpointMappingFactory> factories = delegates.get(sourceType);
        int size = factories == null ? 0 : factories.size();
        if (size < 1) {
            return null;
        }
        Optional<WebEndpointMapping<?>> result = null;
        for (int i = 0; i < size; i++) {
            WebEndpointMappingFactory factory = factories.get(i);
            if (factory.supports(source)) {
                result = factory.create(source);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }
}
