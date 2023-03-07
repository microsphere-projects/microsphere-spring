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
import io.github.microsphere.spring.test.jdbc.embedded.EnableEmbeddedDatabase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;

/**
 * {@link P6DataSourceBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {P6DataSourceBeanPostProcessor.class, P6DataSourceBeanPostProcessorTest.class})
@TestPropertySource(properties = {"microsphere.jdbc.p6spy.excluded-datasource-beans=nonWrappedDataSource"})
@EnableEmbeddedDatabase(dataSource = "nonWrappedDataSource")
@EnableEmbeddedDatabase(dataSource = "wrappedDataSource")
public class P6DataSourceBeanPostProcessorTest {

    @Autowired
    @Qualifier("nonWrappedDataSource")
    private DataSource nonWrappedDataSource;

    @Autowired
    @Qualifier("wrappedDataSource")
    private DataSource wrappedDataSource;

    @Test
    public void test() throws Exception {
        assertEquals(DriverManagerDataSource.class, nonWrappedDataSource.getClass());
        assertEquals(P6DataSource.class, wrappedDataSource.getClass());
        assertEquals(DriverManagerDataSource.class, wrappedDataSource.unwrap(DataSource.class).getClass());
    }
}
