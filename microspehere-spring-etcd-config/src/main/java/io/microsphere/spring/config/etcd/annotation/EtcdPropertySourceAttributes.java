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
package io.microsphere.spring.config.etcd.annotation;

import io.microsphere.spring.config.context.annotation.PropertySourceExtensionAttributes;
import org.springframework.core.env.PropertyResolver;

import java.util.Map;

/**
 * The {@link ResourcePropertySourceAttributes} for {@link EtcdPropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EtcdPropertySource
 * @see ResourcePropertySourceAttributes
 * @since 1.0.0
 */
public class EtcdPropertySourceAttributes extends PropertySourceExtensionAttributes<EtcdPropertySource> {

    public EtcdPropertySourceAttributes(Map<String, Object> another, Class<EtcdPropertySource> annotationType, PropertyResolver propertyResolver) {
        super(another, annotationType, propertyResolver);
    }

    public final String getTarget() {
        return getString("target");
    }

    public final String[] getEndpoints() {
        return getStringArray("endpoints");
    }
}
