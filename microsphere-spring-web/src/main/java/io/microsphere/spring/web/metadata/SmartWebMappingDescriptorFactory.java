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

import io.microsphere.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;

/**
 * The smart {@link WebMappingDescriptorFactory} class based on Spring's {@link WebMappingDescriptorFactory} SPI
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SmartWebMappingDescriptorFactory implements WebMappingDescriptorFactory<Object> {

    private final static Logger logger = LoggerFactory.getLogger(SmartWebMappingDescriptorFactory.class);

    private final Map<Class<?>, WebMappingDescriptorFactory<?>> delegates;

    public SmartWebMappingDescriptorFactory() {
        this(null);
    }

    public SmartWebMappingDescriptorFactory(@Nullable ClassLoader classLoader) {
        this.delegates = loadDelegates(classLoader);
    }

    private Map<Class<?>, WebMappingDescriptorFactory<?>> loadDelegates(@Nullable ClassLoader classLoader) {
        ClassLoader targetClassLoader = classLoader == null ? ClassLoaderUtils.getDefaultClassLoader() : classLoader;
        List<String> factoryClassNames = loadFactoryNames(WebMappingDescriptorFactory.class, targetClassLoader);
        int size = factoryClassNames.size();
        Map<Class<?>, WebMappingDescriptorFactory<?>> delegates = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            String factoryClassName = factoryClassNames.get(i);
            WebMappingDescriptorFactory<?> delegate = createDelegate(factoryClassName, targetClassLoader);
            if (delegate != null) {
                Class<?> sourceType = delegate.getSourceType();
                delegates.put(sourceType, delegate);
            }
        }
        return delegates;
    }

    private WebMappingDescriptorFactory<?> createDelegate(String factoryClassName,
                                                          ClassLoader targetClassLoader) {
        WebMappingDescriptorFactory<?> factory = null;
        try {
            Class<?> factoryClass = targetClassLoader.loadClass(factoryClassName);
            factory = (WebMappingDescriptorFactory<?>) factoryClass.newInstance();
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
        WebMappingDescriptorFactory delegate = delegates.get(sourceType);
        return delegate == null ? null : delegate.create(source);
    }
}
