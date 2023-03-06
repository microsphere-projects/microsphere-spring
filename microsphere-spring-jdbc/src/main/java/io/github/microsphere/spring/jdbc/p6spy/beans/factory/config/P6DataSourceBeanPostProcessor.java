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
package io.github.microsphere.spring.jdbc.p6spy.beans.factory.config;

import com.p6spy.engine.spy.P6DataSource;
import io.github.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

/**
 * {@link P6DataSource} {@link BeanPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see P6DataSource
 * @since 1.0.0
 */
public class P6DataSourceBeanPostProcessor extends GenericBeanPostProcessorAdapter<DataSource> implements EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(P6DataSourceBeanPostProcessor.class);

    public static final String EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME = "microsphere.jdbc.p6spy.excluded-datasource-beans";

    private Set<String> excludedDataSourceBeanNames;

    @Override
    protected DataSource doPostProcessAfterInitialization(DataSource bean, String beanName) throws BeansException {

        DataSource targetDataSource = bean;

        if (excludedDataSourceBeanNames.contains(beanName)) {
            logger.debug("The DataSource bean[name : '{}'] is excluded, it caused by Spring property[name : '{}']", beanName, EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME);
        } else {
            try {
                DataSource datasource = bean.unwrap(DataSource.class);
                targetDataSource = new P6DataSource(datasource);
            } catch (SQLException e) {
                logger.debug("The DataSource bean[name : '{}' , class : '{}'] can't unwrap to be an instance DataSource", beanName, bean.getClass().getName());
            }
        }

        logger.debug("The DataSource bean[name : '{}'] {} -> {}", beanName, bean, targetDataSource);

        return targetDataSource;
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.excludedDataSourceBeanNames = environment.getProperty(EXCLUDED_DATASOURCE_BEAN_NAMES_PROPERTY_NAME, Set.class, Collections.emptySet());
    }
}
