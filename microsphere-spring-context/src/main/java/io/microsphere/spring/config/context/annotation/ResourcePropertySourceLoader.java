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

    /**
     * Initializes the {@link ResourcePatternResolver} and {@link PathMatcher} used for resolving
     * resource patterns after all bean properties have been set.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked automatically by the Spring container:
     *   ResourcePropertySourceLoader loader = new ResourcePropertySourceLoader();
     *   loader.setResourceLoader(applicationContext);
     *   loader.afterPropertiesSet();
     *   // loader is now ready to resolve resource patterns like "classpath*:/META-INF/test/*.properties"
     * }</pre>
     *
     * @throws Exception if initialization fails
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getResourceLoader());
        this.resourcePatternResolver = resolver;
        this.pathMatcher = resolver.getPathMatcher();
    }

    /**
     * Resolves the {@link Resource} instances matching the given resource value pattern
     * using the internal {@link ResourcePatternResolver}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Within a subclass or test:
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attrs = ...;
     *   Resource[] resources = loader.resolveResources(attrs, "myPropertySource",
     *       "classpath*:/META-INF/test/*.properties");
     *   // resources contains all .properties files matching the pattern
     * }</pre>
     *
     * @param extensionAttributes the extension attributes of the {@link ResourcePropertySource} annotation
     * @param propertySourceName  the name of the property source being loaded
     * @param resourceValue       the resource location pattern to resolve (e.g. {@code "classpath*:/META-INF/test/*.properties"})
     * @return an array of resolved {@link Resource} instances matching the pattern
     * @throws Throwable if resource resolution fails
     */
    @Override
    protected Resource[] resolveResources(PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes,
                                          String propertySourceName, String resourceValue) throws Throwable {
        return this.resourcePatternResolver.getResources(resourceValue);
    }

    /**
     * Determines whether the given resource value is a pattern containing wildcards
     * (e.g. {@code *}, {@code ?}) that requires pattern-based resource resolution.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ResourcePropertySourceLoader loader = ...;
     *   boolean isPattern = loader.isResourcePattern("classpath*:/META-INF/test/*.properties"); // true
     *   boolean isNotPattern = loader.isResourcePattern("classpath:/META-INF/test/a.properties"); // false
     * }</pre>
     *
     * @param resourceValue the resource location string to test
     * @return {@code true} if the resource value contains wildcard patterns, {@code false} otherwise
     */
    @Override
    public boolean isResourcePattern(String resourceValue) {
        return pathMatcher.isPattern(resourceValue);
    }

    /**
     * Configures the {@link ResourcePropertySourcesRefresher} by setting up a
     * {@link StandardFileWatchService} that watches file-based resources for changes.
     * When a watched file is created, modified, or deleted, the refresher is triggered
     * to reload the affected property sources.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called internally by the property source loading lifecycle when autoRefreshed = true.
     *   // For example, given the annotation:
     *   // @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties", autoRefreshed = true)
     *   // the loader internally invokes this method to set up file watching on the resolved
     *   // .properties files, so that property sources are refreshed when files change on disk.
     * }</pre>
     *
     * @param extensionAttributes    the extension attributes of the {@link ResourcePropertySource} annotation
     * @param propertySourceResources the list of property source resources to watch
     * @param propertySource         the composite property source that aggregates the individual sources
     * @param refresher              the refresher to invoke when file changes are detected
     * @throws Throwable if configuring the file watch service fails
     */
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

        /**
         * Registers a file and its corresponding resource value for change tracking.
         * The registered file will be monitored for modifications, and the resource value
         * is used to identify which property source to refresh.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   ListenerAdapter adapter = new ListenerAdapter(refresher, 4);
         *   File propertiesFile = new File("/META-INF/test/a.properties");
         *   adapter.register(propertiesFile, "classpath*:/META-INF/test/*.properties");
         * }</pre>
         *
         * @param file          the file to register for change tracking
         * @param resourceValue the resource location value associated with the file
         */
        public void register(File file, String resourceValue) {
            this.fileToResourceValues.put(file, resourceValue);
            this.resourceValues.add(resourceValue);
        }

        /**
         * Handles a file creation event by checking if the newly created file matches any
         * registered resource patterns and triggering a property source refresh if so.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Automatically invoked by the file watch service when a new file is detected:
         *   // e.g., creating "c.properties" in a watched directory triggers a refresh
         *   // if it matches a registered pattern like "classpath*:/META-INF/test/*.properties"
         * }</pre>
         *
         * @param event the file changed event containing the created file information
         */
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

        /**
         * Handles a file modification event by refreshing the property source associated with
         * the modified file.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Automatically invoked by the file watch service when a tracked file is modified:
         *   // e.g., modifying "b.properties" triggers a refresh of its associated property source
         * }</pre>
         *
         * @param event the file changed event containing the modified file information
         */
        @Override
        public void onFileModified(FileChangedEvent event) {
            File resourceFile = event.getFile();
            String resourceValue = fileToResourceValues.get(resourceFile);
            if (resourceValue != null) {
                refreshResource(resourceValue, resourceFile);
            }
        }

        /**
         * Handles a file deletion event by delegating to {@link #onFileModified(FileChangedEvent)},
         * which triggers a refresh of the property source associated with the deleted file.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Automatically invoked by the file watch service when a tracked file is deleted:
         *   // e.g., deleting "a.properties" triggers a refresh and removed properties are reported
         * }</pre>
         *
         * @param event the file changed event containing the deleted file information
         */
        @Override
        public void onFileDeleted(FileChangedEvent event) {
            onFileModified(event);
        }

        void refreshResource(String resourceValue, File resourceFile) {
            Resource resource = new FileSystemResource(resourceFile);
            execute(() -> refresher.refresh(resourceValue, resource));
        }
    }

    /**
     * Stops the internal {@link StandardFileWatchService} if it was started,
     * releasing file watching resources.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked automatically by the Spring container on shutdown:
     *   ResourcePropertySourceLoader loader = ...;
     *   loader.destroy();
     *   // The file watch service is stopped and resources are released
     * }</pre>
     *
     * @throws Exception if stopping the file watch service fails
     */
    @Override
    public void destroy() throws Exception {
        if (fileWatchService != null) {
            fileWatchService.stop();
        }
    }
}
