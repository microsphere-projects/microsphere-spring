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
package io.microsphere.spring.config.zookeeper.annotation;

import io.microsphere.spring.config.context.annotation.PropertySourceExtensionAttributes;
import org.springframework.core.env.PropertyResolver;

import java.util.Map;

/**
 * {@link ResourcePropertySourceAttributes} for {@link ZookeeperPropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public final class ZookeeperPropertySourceAttributes extends PropertySourceExtensionAttributes<ZookeeperPropertySource> {

    public ZookeeperPropertySourceAttributes(Map<String, Object> another, Class<ZookeeperPropertySource> annotationType, PropertyResolver propertyResolver) {
        super(another, annotationType, propertyResolver);
    }

    public String getConnectString() {
        return getString("connectString");
    }

    public String[] getPaths() {
        return getValue();
    }
}
