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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link PropertySourceFactory} implementation that creates {@link PropertySource} instances from JSON resources.
 * <p>
 * This class reads JSON content and converts it into a {@link MapPropertySource}, allowing the properties defined in the
 * JSON resource to be easily integrated into the Spring environment.
 * </p>
 *
 * <h3>Example Usage</h3>
 * Suppose you have a JSON resource with the following content:
 * <pre>{@code
 * {
 *     "app": {
 *         "name": "My Application",
 *         "version": "1.0.0"
 *     }
 * }
 * }</pre>
 *
 * You can use this factory to load the JSON file as a property source:
 * <pre>{@code
 * EncodedResource encodedResource = new EncodedResource(resource);
 * PropertySource propertySource = jsonPropertySourceFactory.createPropertySource("jsonProperties", encodedResource);
 * environment.getPropertySources().addLast(propertySource);
 * }</pre>
 *
 * After adding the property source to the environment, you can access properties like:
 * <ul>
 *     <li>{@code environment.getProperty("app.name")} which returns "My Application"</li>
 *     <li>{@code environment.getProperty("app.version")} which returns "1.0.0"</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceFactory
 * @see PropertySource
 * @see MapPropertySource
 * @since 1.0.0
 */
public class JsonPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map source = objectMapper.readValue(resource.getReader(), LinkedHashMap.class);
        return new MapPropertySource(name, source);
    }
}
