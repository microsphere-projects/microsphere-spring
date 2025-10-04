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
package io.microsphere.spring.config.env.config;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * A {@link YamlProcessor} implementation that processes one or more YAML {@link Resource resources}
 * into a structured map of properties. This class provides the ability to load and process YAML
 * content from given Spring {@link Resource} objects, combining them into a single read-only
 * {@link Map} of configuration properties.
 *
 * <p>
 * <h3>Example Usage</h3>
 * </p>
 *
 * <pre>{@code
 * Resource resource = new ClassPathResource("application.yml");
 * ResourceYamlProcessor processor = new ResourceYamlProcessor(resource);
 * Map<String, Object> config = processor.process();
 * }</pre>
 *
 * <p>
 * This will load the contents of <code>application.yml</code> into a map structure suitable for
 * configuration purposes.
 * </p>
 *
 * <p>
 * Multiple resources can be processed in the same way:
 * </p>
 *
 * <pre>{@code
 * Resource resource1 = new ClassPathResource("application.yml");
 * Resource resource2 = new ClassPathResource("override.yml");
 * ResourceYamlProcessor processor = new ResourceYamlProcessor(resource1, resource2);
 * Map<String, Object> config = processor.process();
 * }</pre>
 *
 * <p>
 * In this case, properties from <code>override.yml</code> will override those from
 * <code>application.yml</code> if there are key conflicts.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ResourceYamlProcessor extends YamlProcessor {

    public ResourceYamlProcessor(Resource resource) {
        super.setResources(resource);
    }

    public ResourceYamlProcessor(Resource... resources) {
        super.setResources(resources);
    }

    /**
     * Process one or more {@link Resource resources} to be {@link Map}
     *
     * @return non-null read-only
     */
    public Map<String, Object> process() {
        Map<String, Object> storage = new LinkedHashMap<>();
        super.process(((properties, map) -> storage.putAll((Map) properties)));
        return unmodifiableMap(storage);
    }

    @Override
    protected Yaml createYaml() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        loaderOptions.setProcessComments(true);
        loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
        loaderOptions.setAllowRecursiveKeys(true);
        DumperOptions dumperOptions = new DumperOptions();
        return new Yaml(new FilteringConstructor(loaderOptions), new Representer(dumperOptions), dumperOptions, loaderOptions);
    }

    private class FilteringConstructor extends Constructor {

        FilteringConstructor(LoaderOptions loaderOptions) {
            super(loaderOptions);
        }

        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            return super.getClassForName(name);
        }
    }

}
