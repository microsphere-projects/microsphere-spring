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
package io.github.microsphere.spring.jdbc.p6spy;

import com.p6spy.engine.spy.P6LoadableOptions;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySources;

import java.util.Map;

import static io.github.microsphere.spring.util.PropertySourcesUtils.getSubProperties;

/**
 * {@link PropertySources} {@link P6LoadableOptions} Adapter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySources
 * @see P6LoadableOptions
 * @since 1.0.0
 */
public class PropertySourcesP6LoadableOptionsAdapter implements P6LoadableOptions {

    private static final String PROPERTY_NAME_PREFIX = "microsphere.jdbc.p6spy.options";

    private final ConfigurableEnvironment environment;

    public PropertySourcesP6LoadableOptionsAdapter(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void load(Map<String, String> options) {
        // DO NOTHING
    }

    @Override
    public Map<String, String> getDefaults() {
        PropertySources propertySources = environment.getPropertySources();
        Map properties = getSubProperties(propertySources, PROPERTY_NAME_PREFIX);
        return properties;
    }
}
