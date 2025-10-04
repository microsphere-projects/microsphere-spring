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
package io.microsphere.spring.config.env.support;

import io.microsphere.spring.config.env.ImmutableMapPropertySource;
import io.microsphere.spring.config.env.annotation.YamlPropertySource;
import io.microsphere.spring.config.env.config.ResourceYamlProcessor;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;

/**
 * A {@link PropertySourceFactory} implementation that creates {@link PropertySource} instances from YAML resources.
 *
 * <p>This factory processes YAML files into a {@link java.util.Map} using {@link ResourceYamlProcessor},
 * and wraps the result in an {@link ImmutableMapPropertySource} to ensure immutability.</p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Configure a Spring Environment to use this factory for loading YAML resources
 * ConfigurableEnvironment environment = context.getEnvironment();
 * environment.setPropertySources(new YamlPropertySourceFactory().createPropertySource("my-config", encodedResource));
 * }</pre>
 *
 * <p>For more information on how YAML resources are processed, see {@link ResourceYamlProcessor}.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see YamlPropertySource
 * @see PropertySourceFactory
 * @see PropertySource
 * @see ResourceYamlProcessor
 * @see ImmutableMapPropertySource
 * @since 1.0.0
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        ResourceYamlProcessor processor = new ResourceYamlProcessor(resource.getResource());
        return new ImmutableMapPropertySource(name, processor.process());
    }
}
