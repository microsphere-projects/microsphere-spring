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

import static java.util.Collections.emptyList;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getDefaultClassLoader;
import static org.springframework.util.StringUtils.hasText;

/**
 * The smart {@link WebMappingDescriptorFactory} class based on Spring's {@link WebMappingDescriptorFactory} SPI
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SmartWebMappingDescriptorFactory implements WebMappingDescriptorFactory<Object> {

    private static final Class<WebMappingDescriptorFactory> FACTORY_CLASS = WebMappingDescriptorFactory.class;
    private final static Logger logger = LoggerFactory.getLogger(FACTORY_CLASS);

    private final Map<Class<?>, List<WebMappingDescriptorFactory>> delegates;


    SmartWebMappingDescriptorFactory() {
        this(null);
    }

    public SmartWebMappingDescriptorFactory(@Nullable ConfigurableListableBeanFactory beanFactory) {
        this.delegates = loadDelegates(beanFactory);
    }

    private Map<Class<?>, List<WebMappingDescriptorFactory>> loadDelegates(@Nullable ConfigurableListableBeanFactory beanFactory) {
        ClassLoader classLoader = getClassLoader(beanFactory);

        List<WebMappingDescriptorFactory> factories = loadFactories(classLoader);
        Collection<WebMappingDescriptorFactory> factoryBeans = getFactoryBeans(beanFactory);

        int size = factories.size() + factoryBeans.size();

        Map<Class<?>, List<WebMappingDescriptorFactory>> delegates = new HashMap<>(size);

        initDelegates(factories, delegates);
        initDelegates(factoryBeans, delegates);

        return delegates;
    }

    private Collection<WebMappingDescriptorFactory> getFactoryBeans(ConfigurableListableBeanFactory beanFactory) {
        return beanFactory == null ? emptyList() : beanFactory.getBeansOfType(FACTORY_CLASS).values();
    }

    private void initDelegates(Collection<WebMappingDescriptorFactory> factories,
                               Map<Class<?>, List<WebMappingDescriptorFactory>> delegates) {
        for (WebMappingDescriptorFactory factory : factories) {
            Class<?> sourceType = factory.getSourceType();
            List<WebMappingDescriptorFactory> factoriesList =
                    delegates.computeIfAbsent(sourceType, t -> new LinkedList<>());
            factoriesList.add(factory);
            AnnotationAwareOrderComparator.sort(factoriesList);
        }
    }

    private List<WebMappingDescriptorFactory> loadFactories(ClassLoader classLoader) {
        List<String> factoryClassNames = loadFactoryNames(FACTORY_CLASS, classLoader);
        int size = factoryClassNames.size();
        if (size == 0) {
            return emptyList();
        }
        List<WebMappingDescriptorFactory> factories = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String factoryClassName = factoryClassNames.get(i);
            if (hasText(factoryClassName)) {
                WebMappingDescriptorFactory factory = createFactory(factoryClassName, classLoader);
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

    private WebMappingDescriptorFactory createFactory(String factoryClassName,
                                                      ClassLoader targetClassLoader) {
        WebMappingDescriptorFactory factory = null;
        try {
            Class<?> factoryClass = targetClassLoader.loadClass(factoryClassName);
            factory = (WebMappingDescriptorFactory) factoryClass.newInstance();
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("The factory class[name :'{}'] can't be instantiated!", factoryClassName);
            }
        }
        return factory;
    }

    @Override
    public WebMappingDescriptor create(Object source) {
        Class<?> sourceType = source.getClass();
        List<WebMappingDescriptorFactory> factories = delegates.get(sourceType);
        int size = factories == null ? 0 : factories.size();
        if (size < 1) {
            return null;
        }
        WebMappingDescriptor descriptor = null;
        for (int i = 0; i < size; i++) {
            WebMappingDescriptorFactory factory = factories.get(i);
            if (factory.supports(source)) {
                descriptor = factory.create(source);
                if (descriptor != null) {
                    break;
                }
            }
        }
        return descriptor;
    }
}
