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
package io.microsphere.spring.config.context.annotation;

import io.microsphere.io.StandardFileWatchService;
import io.microsphere.io.event.FileChangedEvent;
import io.microsphere.io.event.FileChangedListener;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.PathMatcher;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.spring.core.io.ResourceUtils.isFileBasedResource;

/**
 * The {@link PropertySourceExtensionLoader} Class for {@link ResourcePropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @see PropertySourceExtensionAttributes
 * @since 1.0.0
 */
public class ResourcePropertySourceLoader extends PropertySourceExtensionLoader<ResourcePropertySource,
        PropertySourceExtensionAttributes<ResourcePropertySource>> implements InitializingBean, ResourceLoaderAware,
        BeanClassLoaderAware, DisposableBean {

    private ResourcePatternResolver resourcePatternResolver;

    private PathMatcher pathMatcher;

    private StandardFileWatchService fileWatchService;

    @Override
    public void afterPropertiesSet() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getResourceLoader());
        this.resourcePatternResolver = resolver;
        this.pathMatcher = resolver.getPathMatcher();
    }

    @Override
    protected Resource[] resolveResources(PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes,
                                          String propertySourceName, String resourceValue) throws Throwable {
        return this.resourcePatternResolver.getResources(resourceValue);
    }

    @Override
    public boolean isResourcePattern(String resourceValue) {
        return pathMatcher.isPattern(resourceValue);
    }

    @Override
    protected void configureResourcePropertySourcesRefresher(PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes,
                                                             List<PropertySourceResource> propertySourceResources, CompositePropertySource propertySource,
                                                             ResourcePropertySourcesRefresher refresher) throws Throwable {

        this.fileWatchService = new StandardFileWatchService();

        int size = propertySourceResources.size();
        ListenerAdapter listenerAdapter = new ListenerAdapter(refresher, size);

        for (int i = 0; i < size; i++) {
            PropertySourceResource propertySourceResource = propertySourceResources.get(i);
            Resource resource = propertySourceResource.getResource();
            if (isFileBasedResource(resource)) {
                File resourceFile = resource.getFile();
                listenerAdapter.register(resourceFile, propertySourceResource.getResourceValue());
                fileWatchService.watch(resourceFile, listenerAdapter);
            }
        }

        fileWatchService.start();

    }


    class ListenerAdapter implements FileChangedListener {

        private final ResourcePropertySourcesRefresher refresher;

        private final Map<File, String> fileToResourceValues;

        private final Set<String> resourceValues;

        ListenerAdapter(ResourcePropertySourcesRefresher refresher, int initialCapacity) {
            this.refresher = refresher;
            this.fileToResourceValues = new HashMap<>(initialCapacity);
            this.resourceValues = new HashSet<>(initialCapacity);
        }

        public void register(File file, String resourceValue) {
            this.fileToResourceValues.put(file, resourceValue);
            this.resourceValues.add(resourceValue);
        }

        @Override
        public void onFileCreated(FileChangedEvent event) {
            // new file created
            File resourceFile = event.getFile();
            for (String resourceValue : resourceValues) {
                if (isResourcePattern(resourceValue)) {
                    boolean found = execute(() -> {
                        Resource[] resources = resourcePatternResolver.getResources(resourceValue);
                        Resource resource = findResource(resourceFile, resources);
                        if (resource != null) {
                            refresher.refresh(resourceValue, resource);
                            return true;
                        }
                        return false;
                    });
                    if (found) {
                        break;
                    }
                }
            }
        }

        private Resource findResource(File resourceFile, Resource[] resources) throws IOException {
            for (Resource resource : resources) {
                if (resourceFile.equals(resource.getFile())) {
                    return resource;
                }
            }
            return null;
        }

        @Override
        public void onFileModified(FileChangedEvent event) {
            File resourceFile = event.getFile();
            String resourceValue = fileToResourceValues.get(resourceFile);
            if (resourceValue != null) {
                refreshResource(resourceValue, resourceFile);
            }
        }

        @Override
        public void onFileDeleted(FileChangedEvent event) {
            onFileModified(event);
        }

        void refreshResource(String resourceValue, File resourceFile) {
            Resource resource = new FileSystemResource(resourceFile);
            execute(() -> refresher.refresh(resourceValue, resource));
        }
    }

    @Override
    public void destroy() throws Exception {
        if (fileWatchService != null) {
            fileWatchService.stop();
        }
    }
}
