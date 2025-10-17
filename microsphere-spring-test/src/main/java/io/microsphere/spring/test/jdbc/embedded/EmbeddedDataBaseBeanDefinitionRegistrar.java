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

package io.microsphere.spring.test.jdbc.embedded;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.StringJoiner;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * Embedded database {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableEmbeddedDatabase
 * @since 1.0.0
 */
class EmbeddedDataBaseBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = EnableEmbeddedDatabase.class;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
        registerBeanDefinitions(attributes, registry);
    }

    void registerBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        assertBeanName(attributes, registry);
        EmbeddedDatabaseType type = attributes.getEnum("type");
        switch (type) {
            case SQLITE:
                processSQLite(attributes, registry);
                break;
        }
    }

    private void assertBeanName(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        String beanName = attributes.getString("dataSource");
        if (registry.containsBeanDefinition(beanName)) {
            throw new BeanCreationException("The duplicated BeanDefinition with name : " + beanName);
        }
    }

    private void processSQLite(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        registerSQLiteDataSourceBeanDefinition(attributes, registry);
    }

    private void registerSQLiteDataSourceBeanDefinition(AnnotationAttributes attributes,
                                                        BeanDefinitionRegistry registry) {
        registerDataSourceBeanDefinition("jdbc:sqlite::memory:", attributes, registry);
    }


    private void registerDataSourceBeanDefinition(String jdbcURL,
                                                  AnnotationAttributes attributes,
                                                  BeanDefinitionRegistry registry) {
        String beanName = attributes.getString("dataSource");
        boolean primary = attributes.getBoolean("primary");
        Properties properties = resolveProperties(attributes, registry);

        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(DriverManagerDataSource.class);
        beanDefinitionBuilder.addConstructorArgValue(jdbcURL);
        beanDefinitionBuilder.addConstructorArgValue(properties);

        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private Properties resolveProperties(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) registry;
        String[] values = attributes.getStringArray("properties");
        StringJoiner stringJoiner = new StringJoiner(LINE_SEPARATOR);
        for (String value : values) {
            String resolvedValue = beanFactory.resolveEmbeddedValue(value);
            stringJoiner.add(resolvedValue);
        }
        Properties properties = new Properties();
        execute(() -> properties.load(new StringReader(stringJoiner.toString())));
        return properties;
    }
}
